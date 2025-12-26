package com.bojiu.webapp.user.service;

import com.bojiu.common.dao.SmartQueryWrapper;
import com.bojiu.common.model.TimeInterval;
import com.bojiu.common.web.Page;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.AdminLogDao;
import com.bojiu.webapp.user.entity.AdminLog;
import com.bojiu.webapp.user.form.query.OperLogQuery;
import com.bojiu.webapp.user.vo.AdminLogVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminLogService extends BaseServiceImpl<AdminLog, AdminLogDao, Long> {

	/**
	 * 记录管理员的一次操作
	 *
	 * @param log 管理员的操作日志对象
	 */
	@Transactional
	public AdminLog log(AdminLog log) {
		log.initDefault();
		super.save(log);
		return log;
	}

	/**
	 * 获取平台操作日志
	 */
	public Page<AdminLogVO> findPlatformPage(Page<AdminLogVO> page, OperLogQuery query, TimeInterval addTimeInterval) {
		var wrapper = new SmartQueryWrapper<AdminLog>()
				.eq("e.username", query.getOperatorName())
				.likeRight("t.module_name", query.getModuleName())
				.likeRight("t.func", query.getFunc())
				.between("t.add_time", addTimeInterval)
				.orderByDesc("t.id");

		if (query.isAdmin()) {
			wrapper.eq("t.merchant_id", Merchant.PLATFORM_ID);
		} else if (query.getMerchantIds() != null) {
			wrapper.ins("t.merchant_id", query.getMerchantIds());
		} else {
			wrapper.ne("t.merchant_id", Merchant.PLATFORM_ID);
		}
		return baseDao.findPlatformPageByQuery(page, wrapper);
	}

}
