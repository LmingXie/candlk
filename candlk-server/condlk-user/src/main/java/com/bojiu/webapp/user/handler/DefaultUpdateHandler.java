package com.bojiu.webapp.user.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.alibaba.fastjson2.JSONArray;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.config.UserConfig;
import com.bojiu.webapp.user.dto.JsonInfo;
import com.bojiu.webapp.user.entity.*;
import com.bojiu.webapp.user.model.ChatType;
import com.bojiu.webapp.user.model.MsgType;
import com.bojiu.webapp.user.service.ChatService;
import com.bojiu.webapp.user.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.jspecify.annotations.Nullable;

import static com.bojiu.webapp.user.config.UserConfig.IMG_SUFFIX;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Setter
public class DefaultUpdateHandler implements Client.ResultHandler {

	/** 初始化时设置 */
	public Client client;
	public TgUser user;
	private TdApi.AuthorizationState authorizationState = null;
	/** 是否已完成授权 */
	private static volatile boolean haveAuthorization = false;
	/** 授权开始时间（授权成功后将会重置为当前时间） */
	public Date beginTime;
	private static final Cache<Integer, Long> updateChatPhotoTempsCache = Caffeine.newBuilder()
			.initialCapacity(100)
			.maximumSize(1024)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build();

	public DefaultUpdateHandler(TgUser user) {
		this.user = user;
	}

	@Override
	public void onResult(TdApi.Object object) {
		final int msgConstructor = object.getConstructor();
		switch (msgConstructor) {
			case TdApi.UpdateNewChat.CONSTRUCTOR -> { // 添加新对话
				TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
				TdApi.Chat chat = updateNewChat.chat;
				// synchronized (chat) {
				// 	chats.put(chat.id, chat);
				//
				// 	TdApi.ChatPosition[] positions = chat.positions;
				// 	chat.positions = new TdApi.ChatPosition[0];
				// 	setChatPositions(chat, positions);
				// }
			}

			case TdApi.UpdateUserFullInfo.CONSTRUCTOR -> { // 更新用户完整信息
				// TODO: 2025/11/29 更新user_info信息，并同步更新用户的头像信息
				TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
				// 简介
				String text = updateUserFullInfo.userFullInfo.bio.text;

				TdApi.ChatPhoto photo = updateUserFullInfo.userFullInfo.photo;
				// usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
			}
			case TdApi.UpdateChatPhoto.CONSTRUCTOR -> { // 更新对话的照片
				TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
				TdApi.File fileToDownload = updateChat.photo.big;
				TdApi.LocalFile localFile = fileToDownload.local;
				if (localFile.isDownloadingCompleted) {
					// 1. 已下载完成，直接使用本地路径
					String localPath = localFile.path;
					log.info("图片已在本地：" + localPath);
					// TODO: 2025/11/29 可能命中了已下载的图片，此时应该直接更新头像
				} else if (localFile.canBeDownloaded) {
					client.send(new TdApi.DownloadFile(fileToDownload.id, 1, 0, 0, false), obj -> {
						// 请求成功发送，但文件内容下载的完成状态需要监听 UpdateFile 事件
						if (obj instanceof TdApi.File file) {
							if (file.local.isDownloadingCompleted) {
								log.error("异步下载完成：{}", Jsons.encode(file));
								// TODO: 2025/11/29 可能命中了已下载的图片，此时应该直接更新头像
							} else if (file.local.isDownloadingActive) {
								updateChatPhotoTempsCache.put(file.id, updateChat.chatId);
								log.info("正在下载中222...{}", Jsons.encode(file));
							}
						} else {
							log.info("意外的下载事件: {}->{}", obj.getConstructor(), Jsons.encode(obj));
						}
					});
				}
			}
			case TdApi.UpdateFile.CONSTRUCTOR -> {
				TdApi.File file = ((TdApi.UpdateFile) object).file;
				if (file.local.isDownloadingCompleted) {
					Long chatId = updateChatPhotoTempsCache.getIfPresent(file.id);
					if (chatId != null) {
						log.info("异步下载完成，本地路径111：{}", Jsons.encode(file));
						updateChatPhotoTempsCache.invalidate(file.id);
						// TODO: 2025/11/29 更新头像文件路径（调整为重命名并迁移到指定目录）
					}
				}
			}
			case TdApi.UpdateChatLastMessage.CONSTRUCTOR -> { // 更新聊天最后一条消息
				TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
				// TdApi.Chat chat = chats.get(updateChat.chatId);
				// synchronized (chat) {
				// 	chat.lastMessage = updateChat.lastMessage;
				// 	setChatPositions(chat, updateChat.positions);
				// }
			}
			case TdApi.UpdateChatReadInbox.CONSTRUCTOR -> { // 更新对话未读计数
				TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
				// TdApi.Chat chat = chats.get(updateChat.chatId);
				// synchronized (chat) {
				// 	chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
				// 	chat.unreadCount = updateChat.unreadCount;
				// }
			}
			case TdApi.UpdateNewMessage.CONSTRUCTOR -> {
				TdApi.UpdateNewMessage newMsg = (TdApi.UpdateNewMessage) object;
				TdApi.Message msg = newMsg.message;
				log.info("收到新消息：{}->{}", msgConstructor, Jsons.encode(object));
				final long chatId = msg.chatId, msgId = msg.id;
				TdApi.MessageSender sender = msg.senderId;

				MsgType msgType = MsgType.MSG;
				if (msg.replyTo != null) {
					msgType = MsgType.REPLY;
				} else if (msg.forwardInfo != null) {
					msgType = MsgType.FORWARD;
				}

				final long fromId = switch (sender.getConstructor()) {
					case TdApi.MessageSenderUser.CONSTRUCTOR -> ((TdApi.MessageSenderUser) sender).userId;
					case TdApi.MessageSenderChat.CONSTRUCTOR -> ((TdApi.MessageSenderChat) sender).chatId;
					default -> throw new IllegalStateException("Unexpected value: " + sender.getConstructor());
				};
				final TdApi.MessageContent content = msg.content;
				final TgMsg tgMsg = new TgMsg().setUserId(user.getUserId())
						.setType(msgType).setMsgId(msg.id).setGroupId(String.valueOf(msg.mediaAlbumId))
						.setChatId(chatId)
						.setPeerId(TgMsg.toPeerId(chatId))
						.setFromId(fromId)
						.setRaw(Jsons.encode(msg));
				tgMsg.initTime(new Date(msg.date));
				final boolean userMsg = tgMsg.asUserMsg();
				final int contentConstructor = content.getConstructor();
				switch (contentConstructor) {
					case TdApi.MessageText.CONSTRUCTOR -> {
						TdApi.MessageText textMsg = (TdApi.MessageText) content;
						TdApi.FormattedText txt = textMsg.text;
						tgMsg.setMessage(txt.text).setEntities(Jsons.encode(txt.entities));
					}
					case TdApi.MessagePhoto.CONSTRUCTOR -> {
						TdApi.MessagePhoto msgPhoto = (TdApi.MessagePhoto) content;
						tgMsg.setMessage(msgPhoto.caption.text);
						if (userMsg) { // 仅存档私聊消息的图片
							final TdApi.Photo photo = msgPhoto.photo;
							final TdApi.PhotoSize largest = photo.sizes[photo.sizes.length - 1];
							final TdApi.File file = largest.photo;

							// 下载原图
							downloadFile(client, file.id, () -> getUserConfig().imgPath + "\\" + file.remote.uniqueId + IMG_SUFFIX,
									f -> tgMsg.setMedias(Jsons.encode(JSONArray.of("img:" + file.remote.uniqueId))));
						}
					}
					case TdApi.MessageDocument.CONSTRUCTOR -> { // 其他文件消息
						final TdApi.MessageDocument docMsg = (TdApi.MessageDocument) content;
						tgMsg.setMessage(docMsg.caption.text);

						if (userMsg) {
							final TdApi.Document doc = docMsg.document;
							// 下载原文件
							downloadFile(client, doc.document.id, () -> getUserConfig().filePath + "\\" + doc.document.remote.uniqueId,
									f -> tgMsg.setMedias(Jsons.encode(JSONArray.of("file:" + doc.document.remote.uniqueId
											+ ":" + doc.fileName + ":" + TgMsg.convertSize(doc.document.size)))));
						}
					}
					case TdApi.MessageAnimation.CONSTRUCTOR, TdApi.MessageAudio.CONSTRUCTOR -> { // 动图 或 视频
						if (userMsg) {
							final String uniqueId;
							final int fileId;
							if (contentConstructor == TdApi.MessageAnimation.CONSTRUCTOR) {
								TdApi.MessageAnimation animationMsg = (TdApi.MessageAnimation) content;
								tgMsg.setMessage(animationMsg.caption.text);
								TdApi.Animation animation = animationMsg.animation;
								fileId = animation.animation.id;
								uniqueId = animation.animation.remote.uniqueId;
							} else {
								TdApi.MessageAudio audioMsg = (TdApi.MessageAudio) content;
								tgMsg.setMessage(audioMsg.caption.text);
								TdApi.Audio audio = audioMsg.audio;
								fileId = audio.audio.id;
								uniqueId = audio.audio.remote.uniqueId;
							}

							downloadFile(client, fileId, () -> getUserConfig().videoPath + "\\" + uniqueId,
									f -> tgMsg.setMedias(Jsons.encode(JSONArray.of("video:" + uniqueId))));
						}
					}
					default -> {
					}
				}

				log.info("新消息入库：{}", Jsons.encode(tgMsg));

				if (userMsg) {
					// 检查是否已存在对话记录
					final Long peerId = tgMsg.getPeerId();
					final ChatService chatService = getChatService();
					final TgChat chat = chatService.getCache(user.getUserId(), peerId);
					if (chat == null) {
						// 查询用户信息
						final TdApi.User peerInfo = client.sendSync(new TdApi.GetUser(peerId));

						final Date now = new Date();
						final TgChat tgChat = new TgChat()
								.setType(ChatType.PRIVATE)
								.setChatId(chatId)
								.setUserId(user.getUserId())
								.setPeerId(peerId)
								.setTitle(peerInfo.firstName + " " + peerInfo.lastName)
								.setJsonEntity(Jsons.encode(peerInfo));
						final TdApi.ProfilePhoto photo = peerInfo.profilePhoto;
						if (photo != null) {
							downloadFile(client, photo.big.id, () -> getUserConfig().avatarPath + "\\" + photo.big.remote.uniqueId + IMG_SUFFIX,
									f -> tgChat.setAvatar("avatar/" + photo.big.remote.uniqueId + IMG_SUFFIX));
						}
						final UserInfo peer = UserInfo.of(peerId, peerInfo.phoneNumber, parseUsername(peerInfo), parseNickname(peerInfo),
								tgChat.getAvatar(), tgChat.getJsonEntity(), now, peerInfo.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR ? 1 : 0);
						chatService.add(tgChat, peer);
					}
				}
			}
			case TdApi.UpdateAuthorizationState.CONSTRUCTOR -> // 更新授权状态
					onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
			case TdApi.UpdateUser.CONSTRUCTOR -> { // 更新用户
				TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
				// users.put(updateUser.user.id, updateUser.user);
			}
			case TdApi.UpdateUserStatus.CONSTRUCTOR -> { // TODO 更新用户状态，可用于检测内部账号状态
				TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
				TdApi.UserStatus status = updateUserStatus.status;
				log.info("更新用户状态：{}->{}", updateUserStatus.userId, Jsons.encode(status));
				// TdApi.User user = users.get(updateUserStatus.userId);
				// synchronized (user) {
				// 	user.status = updateUserStatus.status;
				// }
			}
			case TdApi.UpdateBasicGroup.CONSTRUCTOR -> { // 更新基本组
				TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
				// basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
			}
			case TdApi.UpdateSupergroup.CONSTRUCTOR -> { // 更新超群
				TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
				// supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
			}
			case TdApi.UpdateSecretChat.CONSTRUCTOR -> { // 更新秘密聊天
				TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
				// secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
			}

			// case TdApi.UpdateChatTitle.CONSTRUCTOR -> { // 更新聊天标题
			// 	TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.title = updateChat.title;
			// 	}
			// }
			// case TdApi.UpdateChatPermissions.CONSTRUCTOR -> { // 更新聊天权限
			// 	TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.permissions = update.permissions;
			// 	}
			// }
			// case TdApi.UpdateChatPosition.CONSTRUCTOR -> { // 更新聊天位置
			// 	TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;
			// 	if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
			// 		break;
			// 	}
			//
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		int i;
			// 		for (i = 0; i < chat.positions.length; i++) {
			// 			if (chat.positions[i].list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
			// 				break;
			// 			}
			// 		}
			// 		TdApi.ChatPosition[] new_positions = new TdApi.ChatPosition[chat.positions.length + (updateChat.position.order == 0 ? 0 : 1) - (i < chat.positions.length ? 1 : 0)];
			// 		int pos = 0;
			// 		if (updateChat.position.order != 0) {
			// 			new_positions[pos++] = updateChat.position;
			// 		}
			// 		for (int j = 0; j < chat.positions.length; j++) {
			// 			if (j != i) {
			// 				new_positions[pos++] = chat.positions[j];
			// 			}
			// 		}
			// 		assert pos == new_positions.length;
			//
			// 		setChatPositions(chat, new_positions);
			// 	}
			// }
			// case TdApi.UpdateChatReadOutbox.CONSTRUCTOR -> { // 更新聊天阅读发件箱
			// 	TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
			// 	}
			// }
			// case TdApi.UpdateChatActionBar.CONSTRUCTOR -> { // 更新聊天操作栏
			// 	TdApi.UpdateChatActionBar updateChat = (TdApi.UpdateChatActionBar) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.actionBar = updateChat.actionBar;
			// 	}
			// }
			// case TdApi.UpdateChatAvailableReactions.CONSTRUCTOR -> { // 更新聊天可用反应
			// 	TdApi.UpdateChatAvailableReactions updateChat = (TdApi.UpdateChatAvailableReactions) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.availableReactions = updateChat.availableReactions;
			// 	}
			// }
			// case TdApi.UpdateChatDraftMessage.CONSTRUCTOR -> { // 更新聊天草稿消息
			// 	TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.draftMessage = updateChat.draftMessage;
			// 		setChatPositions(chat, updateChat.positions);
			// 	}
			// }
			// case TdApi.UpdateChatMessageSender.CONSTRUCTOR -> { // 更新聊天消息发送者
			// 	TdApi.UpdateChatMessageSender updateChat = (TdApi.UpdateChatMessageSender) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.messageSenderId = updateChat.messageSenderId;
			// 	}
			// }
			// case TdApi.UpdateChatMessageAutoDeleteTime.CONSTRUCTOR -> { // 更新聊天消息自动删除时间
			// 	TdApi.UpdateChatMessageAutoDeleteTime updateChat = (TdApi.UpdateChatMessageAutoDeleteTime) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.messageAutoDeleteTime = updateChat.messageAutoDeleteTime;
			// 	}
			// }
			// case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR -> { // 更新聊天通知设置
			// 	TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.notificationSettings = update.notificationSettings;
			// 	}
			// }
			// case TdApi.UpdateChatPendingJoinRequests.CONSTRUCTOR -> { // 更新聊天挂起的连接请求
			// 	TdApi.UpdateChatPendingJoinRequests update = (TdApi.UpdateChatPendingJoinRequests) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.pendingJoinRequests = update.pendingJoinRequests;
			// 	}
			// }
			// case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR -> { // 更新聊天回复标记
			// 	TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
			// 	}
			// }
			// case TdApi.UpdateChatBackground.CONSTRUCTOR -> { // 更新聊天背景
			// 	TdApi.UpdateChatBackground updateChat = (TdApi.UpdateChatBackground) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.background = updateChat.background;
			// 	}
			// }
			// case TdApi.UpdateChatTheme.CONSTRUCTOR -> { // 更新聊天主题
			// 	TdApi.UpdateChatTheme updateChat = (TdApi.UpdateChatTheme) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.theme = updateChat.theme;
			// 	}
			// }
			// case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR -> { // 更新聊天未读提及计数
			// 	TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadMentionCount = updateChat.unreadMentionCount;
			// 	}
			// }
			// case TdApi.UpdateChatUnreadReactionCount.CONSTRUCTOR -> { // 更新聊天未读反应计数。
			// 	TdApi.UpdateChatUnreadReactionCount updateChat = (TdApi.UpdateChatUnreadReactionCount) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadReactionCount = updateChat.unreadReactionCount;
			// 	}
			// }
			// case TdApi.UpdateChatVideoChat.CONSTRUCTOR -> { // 视频聊天状态发生变化。
			// 	TdApi.UpdateChatVideoChat updateChat = (TdApi.UpdateChatVideoChat) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.videoChat = updateChat.videoChat;
			// 	}
			// }
			// case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR -> { // 将消息发送到聊天时使用的默认disableNotification参数的值被更改。
			// 	TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.defaultDisableNotification = update.defaultDisableNotification;
			// 	}
			// }
			// case TdApi.UpdateChatHasProtectedContent.CONSTRUCTOR -> { // 允许或限制保存聊天内容。
			// 	TdApi.UpdateChatHasProtectedContent updateChat = (TdApi.UpdateChatHasProtectedContent) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.hasProtectedContent = updateChat.hasProtectedContent;
			// 	}
			// }
			// case TdApi.UpdateChatIsTranslatable.CONSTRUCTOR -> { // 更新聊天是可翻译的
			// 	TdApi.UpdateChatIsTranslatable update = (TdApi.UpdateChatIsTranslatable) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.isTranslatable = update.isTranslatable;
			// 	}
			// }
			// case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR -> { // 更新聊天被标记为未读
			// 	TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.isMarkedAsUnread = update.isMarkedAsUnread;
			// 	}
			// }
			// case TdApi.UpdateChatBlockList.CONSTRUCTOR -> { // 更新聊天块列表
			// 	TdApi.UpdateChatBlockList update = (TdApi.UpdateChatBlockList) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.blockList = update.blockList;
			// 	}
			// }
			// case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR -> { // 更新聊天已安排消息 聊天的hasScheduledMessages字段已经更改。
			// 	TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.hasScheduledMessages = update.hasScheduledMessages;
			// 	}
			// }
			// case TdApi.UpdateMessageMentionRead.CONSTRUCTOR -> { // 更新消息提及read
			// 	TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadMentionCount = updateChat.unreadMentionCount;
			// 	}
			// }
			// case TdApi.UpdateMessageUnreadReactions.CONSTRUCTOR -> { // 更新消息未读反应
			// 	TdApi.UpdateMessageUnreadReactions updateChat = (TdApi.UpdateMessageUnreadReactions) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadReactionCount = updateChat.unreadReactionCount;
			// 	}
			// }
			// case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR -> {// 更新基本组的完整信息
			// 	TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
			// 	basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
			// }
			// case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR -> { // 更新超组的完整信息
			// 	TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
			// 	supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
			// }
			case TdApi.Error.CONSTRUCTOR -> log.error("收到TDLib错误:" + Jsons.encode(object));
			default -> log.debug("不支持的更新事件:" + Jsons.encode(object));
		}
	}

	private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
		if (authorizationState != null) {
			this.authorizationState = authorizationState;
		}
		JsonInfo jsonInfo = user.jsonInfo();
		switch (this.authorizationState.getConstructor()) {
			case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
				/*
				TdApi.GetOption
					online：当前用户的在线状态
					authentication_token：用于后续授权的身份验证令牌
					my_id：当前用户ID
					enabled_proxy_id：已启用代理ID
				 */
				client.send(new TdApi.GetOption("enabled_proxy_id")/*查询当前使用的代理ID*/, obj -> {
					List<String> proxyInfo = jsonInfo.proxy;
					if (obj instanceof TdApi.OptionValueEmpty) {
						client.send(new TdApi.AddProxy(proxyInfo.get(1), Integer.parseInt(proxyInfo.get(2)), true, jsonInfo.proxyType()), AuthorizationRequestHandler.getInstance());
					} else if (obj instanceof TdApi.OptionValueInteger proxyId) { // 代理已存在，进行修改操作
						// client.send(new TdApi.PingProxy(2), obj1 -> { });
						client.send(new TdApi.EditProxy((int) proxyId.value, proxyInfo.get(1), Integer.parseInt(proxyInfo.get(2)), true, jsonInfo.proxyType()), AuthorizationRequestHandler.getInstance());
					}
				});

				TdApi.SetTdlibParameters request = new TdApi.SetTdlibParameters();
				request.databaseDirectory = user.getPhone(); // 设置数据库目录
				// request.databaseEncryptionKey = "123456".getBytes(StandardCharsets.UTF_8);
				request.useMessageDatabase = false; // 不使用TDLib的消息数据库
				request.useSecretChats = true; // 使用私密聊天
				UserConfig config = getUserConfig();
				request.apiId = config.apiId;
				request.apiHash = config.apiHash;
				request.systemLanguageCode = X.expectNotEmpty(jsonInfo.systemLangCode, "en");
				request.deviceModel = X.expectNotEmpty(jsonInfo.deviceModel, "Desktop");
				request.applicationVersion = X.expectNotEmpty(jsonInfo.appVersion, "1.0");
				request.systemVersion = X.expectNotEmpty(jsonInfo.appVersion, "Windows 11");
				client.send(request, this);
			}
			// 输入手机号
			case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
				this.beginTime = new EasyDate().addMinute(1).toDate();
				log.info("请输入手机号：userId={}，phone={}", user.getUserId(), user.getPhone());
				client.send(new TdApi.SetAuthenticationPhoneNumber(user.getPhone(), null), AuthorizationRequestHandler.getInstance());
			}
			// 输入验证码
			case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
				log.info("等待验证码：userId={}", user.getUserId());
				if (beginTime == null) {
					this.beginTime = new EasyDate().addMinute(20).toDate();
				}
				getUserService().addWaitAuthTask(user.getUserId(), this);
			}
			// 输入密码
			case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
				log.info("输入密码：userId={}", user.getUserId());
				client.send(new TdApi.CheckAuthenticationPassword(jsonInfo.password), AuthorizationRequestHandler.getInstance());
			}
			// 授权成功
			case TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
				getUserService().putClient(user.getUserId(), client);
				haveAuthorization = true;
				beginTime = new Date();
				log.info("授权成功：{}", user.getUserId());
			}
			// 退出登录/掉授权
			case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR, TdApi.AuthorizationStateClosing.CONSTRUCTOR, TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
				haveAuthorization = false;
				log.info("已退出：{}", user.getUserId());
			}
			case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
				String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.authorizationState).link;
				log.warn("请在另一台设备上确认此登录链接: " + link);
			}
			case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR -> {
				// String emailAddress = promptString("请输入电子邮件地址: ");
				// client.send(new TdApi.SetAuthenticationEmailAddress(emailAddress), AuthorizationRequestHandler.getInstance());
				log.warn("不支持 AuthorizationStateWaitEmailAddress 请输入电子邮件地址：{}", user.getUserId());
			}
			case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR -> {
				// String code = promptString("请输入电子邮件授权验证码: ");
				// client.send(new TdApi.CheckAuthenticationEmailCode(new TdApi.EmailAddressAuthenticationCode(code)), AuthorizationRequestHandler.getInstance());
				log.warn("不支持 AuthorizationStateWaitEmailCode 请输入电子邮件授权验证码：{}", user.getUserId());
			}
			case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR -> {
				// String firstName = promptString("请输入您的名字: ");
				// String lastName = promptString("请输入您的姓: ");
				log.warn("不支持【注册】 AuthorizationStateWaitRegistration 请输入您的名字和姓：{}", user.getUserId());
				client.send(new TdApi.RegisterUser(jsonInfo.firstName, jsonInfo.lastName, false), AuthorizationRequestHandler.getInstance());
			}
			// 可以忽略的更新事件
			case TdApi.UpdateConnectionState.CONSTRUCTOR -> {
			}
			default -> log.error("不支持的响应数据类型:{}", Jsons.encode(authorizationState));
		}
	}

	public static TdApi.File downloadFile(Client client, int fileId, Supplier<String> path, @Nullable Consumer<TdApi.File> fileConsumer) {
		final TdApi.File downloaded = client.sendSync(new TdApi.DownloadFile(fileId, 1, 0, 0, true));
		if (downloaded.local.isDownloadingCompleted) {
			try {
				Files.copy(Paths.get(downloaded.local.path), Paths.get(path.get()), REPLACE_EXISTING);
				if (fileConsumer != null) {
					fileConsumer.accept(downloaded);
				}
				return downloaded;
			} catch (IOException ignore) {
			}
		}
		return null;
	}

	/** 解析用户名 */
	public static String parseUsername(TdApi.User userInfo) {
		if (userInfo.usernames != null && userInfo.usernames.activeUsernames.length > 0) {
			return userInfo.usernames.activeUsernames[0];
		}
		return null;
	}

	/** 解析用户昵称 */
	public static String parseNickname(TdApi.User userInfo) {
		if (StringUtil.notBlank(userInfo.firstName) || StringUtil.notBlank(userInfo.lastName)) {
			return userInfo.firstName + " " + userInfo.lastName;
		} else if (userInfo.usernames != null && userInfo.usernames.activeUsernames.length > 0) {
			return userInfo.usernames.activeUsernames[0];
		}
		return "user_" + userInfo.id;
	}

	transient UserConfig _userConfig;

	public UserConfig getUserConfig() {
		return _userConfig == null ? _userConfig = SpringUtil.getBean(UserConfig.class) : _userConfig;
	}

	transient UserService _userService;

	public UserService getUserService() {
		return _userService == null ? _userService = SpringUtil.getBean(UserService.class) : _userService;
	}

	transient ChatService _chatService;

	public ChatService getChatService() {
		return _chatService == null ? _chatService = SpringUtil.getBean(ChatService.class) : _chatService;
	}

}