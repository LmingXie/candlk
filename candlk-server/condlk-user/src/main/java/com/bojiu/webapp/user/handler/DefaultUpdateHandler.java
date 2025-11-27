package com.bojiu.webapp.user.handler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.config.UserConfig;
import com.bojiu.webapp.user.dto.JsonInfo;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.service.UserService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.X;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

@Slf4j
@Setter
public class DefaultUpdateHandler implements Client.ResultHandler {

	/** 初始化时设置 */
	private Client client;
	private User user;
	private TdApi.AuthorizationState authorizationState = null;
	/** 是否已完成授权 */
	private static volatile boolean haveAuthorization = false;

	public DefaultUpdateHandler(User user) {
		this.user = user;
	}

	@Override
	public void onResult(TdApi.Object object) {
		switch (object.getConstructor()) {
			case TdApi.UpdateNewMessage.CONSTRUCTOR:
				TdApi.UpdateNewMessage newMsg = (TdApi.UpdateNewMessage) object;
				TdApi.Message msg = newMsg.message;
				long chatId = msg.chatId;
				TdApi.MessageContent content = msg.content;
				if (content instanceof TdApi.MessageText text) {
					TdApi.FormattedText txt = text.text;
					String textStr = txt.text;
					TdApi.TextEntity[] entities = txt.entities;
				} else if (content instanceof TdApi.MessagePhoto photo) {
					TdApi.Photo photo1 = photo.photo;
				} else if (content instanceof TdApi.MessageAnimation animation) {
					TdApi.Animation animation1 = animation.animation;
				} else if (content instanceof TdApi.MessageDocument document) {
					TdApi.Document document1 = document.document;
				} else if (content instanceof TdApi.MessageSticker sticker) {
					TdApi.Sticker sticker1 = sticker.sticker;
				} else if (content instanceof TdApi.MessageAudio audio) {
					TdApi.Audio audio1 = audio.audio;
				}
				long mediaAlbumId = msg.mediaAlbumId;

				long msgId = msg.id;
				TdApi.MessageSender sender = msg.senderId;
				long senderId = switch (sender.getConstructor()) {
					case TdApi.MessageSenderUser.CONSTRUCTOR -> ((TdApi.MessageSenderUser) sender).userId;
					case TdApi.MessageSenderChat.CONSTRUCTOR -> ((TdApi.MessageSenderChat) sender).chatId;
					default -> throw new IllegalStateException("Unexpected value: " + sender.getConstructor());
				};

				// 查询用户信息
				TdApi.User senderInfo = client.sendSync(new TdApi.GetUser(senderId), 10, TimeUnit.SECONDS);
				TdApi.UserStatus status = senderInfo.status;
				TdApi.UserType type = senderInfo.type;

				// 引用回复
				TdApi.MessageReplyTo replyTo = msg.replyTo;
				// 转发信息
				TdApi.MessageForwardInfo forwardInfo = msg.forwardInfo;
				break;
			case TdApi.UpdateAuthorizationState.CONSTRUCTOR: // 更新授权状态
				onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
				break;
			case TdApi.UpdateUser.CONSTRUCTOR: // 更新用户
				TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
				// users.put(updateUser.user.id, updateUser.user);
				break;
			case TdApi.UpdateUserStatus.CONSTRUCTOR: { // TODO 更新用户状态，可用于检测内部账号状态
				TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
				// TdApi.User user = users.get(updateUserStatus.userId);
				// synchronized (user) {
				// 	user.status = updateUserStatus.status;
				// }
				break;
			}
			case TdApi.UpdateBasicGroup.CONSTRUCTOR: // 更新基本组
				TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
				// basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
				break;
			case TdApi.UpdateSupergroup.CONSTRUCTOR: // 更新超群
				TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
				// supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
				break;
			case TdApi.UpdateSecretChat.CONSTRUCTOR: // 更新秘密聊天
				TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
				// secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
				break;

			case TdApi.UpdateNewChat.CONSTRUCTOR: { // 更新新聊天
				TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
				TdApi.Chat chat = updateNewChat.chat;
				// synchronized (chat) {
				// 	chats.put(chat.id, chat);
				//
				// 	TdApi.ChatPosition[] positions = chat.positions;
				// 	chat.positions = new TdApi.ChatPosition[0];
				// 	setChatPositions(chat, positions);
				// }
				break;
			}
			// case TdApi.UpdateChatTitle.CONSTRUCTOR: { // 更新聊天标题
			// 	TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.title = updateChat.title;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatPhoto.CONSTRUCTOR: { // 更新聊天照片
			// 	TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.photo = updateChat.photo;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatPermissions.CONSTRUCTOR: { // 更新聊天权限
			// 	TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.permissions = update.permissions;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatLastMessage.CONSTRUCTOR: { // 更新聊天最后一条消息
			// 	TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.lastMessage = updateChat.lastMessage;
			// 		setChatPositions(chat, updateChat.positions);
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatPosition.CONSTRUCTOR: { // 更新聊天位置
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
			// 	break;
			// }
			// case TdApi.UpdateChatReadInbox.CONSTRUCTOR: { // 更新聊天阅读收件箱
			// 	TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
			// 		chat.unreadCount = updateChat.unreadCount;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: { // 更新聊天阅读发件箱
			// 	TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatActionBar.CONSTRUCTOR: { // 更新聊天操作栏
			// 	TdApi.UpdateChatActionBar updateChat = (TdApi.UpdateChatActionBar) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.actionBar = updateChat.actionBar;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatAvailableReactions.CONSTRUCTOR: { // 更新聊天可用反应
			// 	TdApi.UpdateChatAvailableReactions updateChat = (TdApi.UpdateChatAvailableReactions) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.availableReactions = updateChat.availableReactions;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: { // 更新聊天草稿消息
			// 	TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.draftMessage = updateChat.draftMessage;
			// 		setChatPositions(chat, updateChat.positions);
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatMessageSender.CONSTRUCTOR: { // 更新聊天消息发送者
			// 	TdApi.UpdateChatMessageSender updateChat = (TdApi.UpdateChatMessageSender) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.messageSenderId = updateChat.messageSenderId;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatMessageAutoDeleteTime.CONSTRUCTOR: { // 更新聊天消息自动删除时间
			// 	TdApi.UpdateChatMessageAutoDeleteTime updateChat = (TdApi.UpdateChatMessageAutoDeleteTime) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.messageAutoDeleteTime = updateChat.messageAutoDeleteTime;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: { // 更新聊天通知设置
			// 	TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.notificationSettings = update.notificationSettings;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatPendingJoinRequests.CONSTRUCTOR: { // 更新聊天挂起的连接请求
			// 	TdApi.UpdateChatPendingJoinRequests update = (TdApi.UpdateChatPendingJoinRequests) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.pendingJoinRequests = update.pendingJoinRequests;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: { // 更新聊天回复标记
			// 	TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatBackground.CONSTRUCTOR: { // 更新聊天背景
			// 	TdApi.UpdateChatBackground updateChat = (TdApi.UpdateChatBackground) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.background = updateChat.background;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatTheme.CONSTRUCTOR: { // 更新聊天主题
			// 	TdApi.UpdateChatTheme updateChat = (TdApi.UpdateChatTheme) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.theme = updateChat.theme;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: { // 更新聊天未读提及计数
			// 	TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadMentionCount = updateChat.unreadMentionCount;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatUnreadReactionCount.CONSTRUCTOR: { // 更新聊天未读反应计数。
			// 	TdApi.UpdateChatUnreadReactionCount updateChat = (TdApi.UpdateChatUnreadReactionCount) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadReactionCount = updateChat.unreadReactionCount;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatVideoChat.CONSTRUCTOR: { // 视频聊天状态发生变化。
			// 	TdApi.UpdateChatVideoChat updateChat = (TdApi.UpdateChatVideoChat) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.videoChat = updateChat.videoChat;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: { // 将消息发送到聊天时使用的默认disableNotification参数的值被更改。
			// 	TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.defaultDisableNotification = update.defaultDisableNotification;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatHasProtectedContent.CONSTRUCTOR: { // 允许或限制保存聊天内容。
			// 	TdApi.UpdateChatHasProtectedContent updateChat = (TdApi.UpdateChatHasProtectedContent) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.hasProtectedContent = updateChat.hasProtectedContent;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatIsTranslatable.CONSTRUCTOR: { // 更新聊天是可翻译的
			// 	TdApi.UpdateChatIsTranslatable update = (TdApi.UpdateChatIsTranslatable) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.isTranslatable = update.isTranslatable;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: { // 更新聊天被标记为未读
			// 	TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.isMarkedAsUnread = update.isMarkedAsUnread;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatBlockList.CONSTRUCTOR: { // 更新聊天块列表
			// 	TdApi.UpdateChatBlockList update = (TdApi.UpdateChatBlockList) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.blockList = update.blockList;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR: { // 更新聊天已安排消息 聊天的hasScheduledMessages字段已经更改。
			// 	TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
			// 	TdApi.Chat chat = chats.get(update.chatId);
			// 	synchronized (chat) {
			// 		chat.hasScheduledMessages = update.hasScheduledMessages;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: { // 更新消息提及read
			// 	TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadMentionCount = updateChat.unreadMentionCount;
			// 	}
			// 	break;
			// }
			// case TdApi.UpdateMessageUnreadReactions.CONSTRUCTOR: { // 更新消息未读反应
			// 	TdApi.UpdateMessageUnreadReactions updateChat = (TdApi.UpdateMessageUnreadReactions) object;
			// 	TdApi.Chat chat = chats.get(updateChat.chatId);
			// 	synchronized (chat) {
			// 		chat.unreadReactionCount = updateChat.unreadReactionCount;
			// 	}
			// 	break;
			// }
			//
			// case TdApi.UpdateUserFullInfo.CONSTRUCTOR: // 更新用户完整信息
			// 	TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
			// 	usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
			// 	break;
			// case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR: // 更新基本组的完整信息
			// 	TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
			// 	basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
			// 	break;
			// case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR: // 更新超组的完整信息
			// 	TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
			// 	supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
			// 	break;
			case TdApi.Error.CONSTRUCTOR:
				log.error("收到TDLib错误:" + Jsons.encode(object));
				break;
			default:
				log.info("不支持的更新事件:" + Jsons.encode(object));
		}
	}

	private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
		if (authorizationState != null) {
			this.authorizationState = authorizationState;
		}
		JsonInfo jsonInfo = user.jsonInfo();
		switch (this.authorizationState.getConstructor()) {
			case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
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
						client.send(new TdApi.AddProxy(proxyInfo.get(1), Integer.parseInt(proxyInfo.get(2)), true, jsonInfo.proxyType()), getAuthorizationRequestHandler());
					} else if (obj instanceof TdApi.OptionValueInteger proxyId) { // 代理已存在，进行修改操作
						// client.send(new TdApi.PingProxy(2), obj1 -> { });
						client.send(new TdApi.EditProxy((int) proxyId.value, proxyInfo.get(1), Integer.parseInt(proxyInfo.get(2)), true, jsonInfo.proxyType()), getAuthorizationRequestHandler());
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
				break;
			case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
				client.send(new TdApi.SetAuthenticationPhoneNumber(user.getPhone(), null), getAuthorizationRequestHandler());
				break;
			}
			case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
				// String code = promptString("请输入授权验证码: "); TODO 需要不断查询数据是否收到验证码，超时回收客户端释放资源
				// client.send(new TdApi.CheckAuthenticationCode(code), getAuthorizationRequestHandler());
				break;
			}
			case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
				client.send(new TdApi.CheckAuthenticationPassword(jsonInfo.password), getAuthorizationRequestHandler());
				break;
			}
			case TdApi.AuthorizationStateReady.CONSTRUCTOR:
				haveAuthorization = true;
				break;
			case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
				haveAuthorization = false;
				log.info("已登出：{}", user.getUserId());
				break;
			case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
			case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
				haveAuthorization = false;
				log.info("已关闭：{}", user.getUserId());
				break;
			case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
				String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.authorizationState).link;
				log.warn("请在另一台设备上确认此登录链接: " + link);
				break;
			}
			case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR: {
				// String emailAddress = promptString("请输入电子邮件地址: ");
				// client.send(new TdApi.SetAuthenticationEmailAddress(emailAddress), getAuthorizationRequestHandler());
				log.warn("不支持 AuthorizationStateWaitEmailAddress 请输入电子邮件地址：{}", user.getUserId());
				break;
			}
			case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR: {
				// String code = promptString("请输入电子邮件授权验证码: ");
				// client.send(new TdApi.CheckAuthenticationEmailCode(new TdApi.EmailAddressAuthenticationCode(code)), getAuthorizationRequestHandler());
				log.warn("不支持 AuthorizationStateWaitEmailCode 请输入电子邮件授权验证码：{}", user.getUserId());
				break;
			}
			case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
				// String firstName = promptString("请输入您的名字: ");
				// String lastName = promptString("请输入您的姓: ");
				log.warn("不支持【注册】 AuthorizationStateWaitRegistration 请输入您的名字和姓：{}", user.getUserId());
				client.send(new TdApi.RegisterUser(jsonInfo.firstName, jsonInfo.lastName, false), getAuthorizationRequestHandler());
				break;
			}
			default:
				log.error("不支持的响应数据类型:{}", Jsons.encode(authorizationState));
		}
	}

	transient AuthorizationRequestHandler authorizationRequestHandler;

	public AuthorizationRequestHandler getAuthorizationRequestHandler() {
		return authorizationRequestHandler == null ? authorizationRequestHandler = new AuthorizationRequestHandler() : authorizationRequestHandler;
	}

	transient UserConfig userConfig;

	public UserConfig getUserConfig() {
		return userConfig == null ? userConfig = SpringUtil.getBean(UserConfig.class) : userConfig;
	}

	transient UserService userService;

	public UserService getUserService() {
		return userService == null ? userService = SpringUtil.getBean(UserService.class) : userService;
	}

}