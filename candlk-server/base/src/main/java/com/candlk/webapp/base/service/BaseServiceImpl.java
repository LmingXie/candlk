package com.candlk.webapp.base.service;

import java.io.Serializable;
import java.util.*;
import javax.annotation.*;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.candlk.common.model.*;
import com.candlk.common.util.SpringUtil;
import com.candlk.common.web.Page;
import com.candlk.context.auth.PermissionException;
import com.candlk.context.model.WithMerchant;
import com.candlk.webapp.base.dao.BaseDao;
import com.candlk.webapp.base.entity.BaseEntity;
import com.candlk.webapp.base.entity.BizEntity;
import me.codeplayer.util.Assert;
import me.codeplayer.util.X;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class BaseServiceImpl<T extends Bean<K>, D extends BaseDao<T>, K extends Serializable> implements BaseService<T, K> {

	static final Log log = LogFactory.getLog(BaseService.class);

	private static SqlSessionFactory sqlSessionFactory;

	protected D baseDao;

	protected final Class<T> entityClass;
	protected final Class<D> mapperClass;

	@SuppressWarnings("unchecked")
	public BaseServiceImpl() {
		final Class<?>[] typeArguments = GenericTypeUtils.resolveTypeArguments(getClass(), BaseServiceImpl.class);
		entityClass = (Class<T>) typeArguments[0];
		mapperClass = (Class<D>) typeArguments[1];
	}

	protected D getBaseDao() {
		return baseDao;
	}

	@Autowired
	public void setBaseDao(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") D baseDao) {
		this.baseDao = baseDao;
	}

	private static SqlSessionFactory getSqlSessionFactory() {
		if (sqlSessionFactory == null) {
			sqlSessionFactory = SpringUtil.getBeanByNameFirst(SqlSessionFactory.class);
		}
		return sqlSessionFactory;
	}

	@Resource
	public void setSqlSessionFactory(SqlSessionFactory factory) {
		sqlSessionFactory = factory;
	}

	protected static void checkIdRequired(ID entity) {
		Assert.isTrue(ID.hasId(entity));
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public T get(K id) {
		if (id == null) {
			return null;
		}
		return baseDao.selectById(id);
	}

	/**
	 * 根据 指定的 主键ID 和 商户ID 查询符合条件的实体
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public T getByIdAndMid(K id, Long merchantId) {
		if (id == null) {
			return null;
		}
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq(BaseEntity.ID, id)
				.eq(WithMerchant.MERCHANT_ID, merchantId);
		return selectOne(wrapper);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public T getAny() {
		return getBaseDao().selectOne(new QueryWrapper<T>().last("LIMIT 1"));
	}

	@Override
	public T getByIdForUpdate(K id) {
		if (id == null) {
			return null;
		}
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq("id", id)
				.last(" FOR UPDATE");

		return baseDao.selectOne(wrapper);
	}

	/**
	 * 根据主键ID数组获取对应的实体数据
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<T> findByIds(K... ids) {
		return findByIds(Arrays.asList(ids));
	}

	/**
	 * 根据主键ID数组获取对应的实体数据
	 */
	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<T> findByIds(Collection<K> ids) {
		if (ids.isEmpty()) {
			return new ArrayList<>();
		}
		return baseDao.selectBatchIds(ids);
	}

	protected List<T> findByField(String field, Object value) {
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq(field, value);
		return baseDao.selectList(wrapper);
	}

	/**
	 * 根据指定的字段获取对应的唯一的持久化实例。<br>
	 * 如果没有对应的实例，则返回 null。
	 */
	protected T getUniqueByField(String uniqueField, Object value) {
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq(uniqueField, value);
		return baseDao.selectOne(wrapper);
	}

	/**
	 * 根据 用户ID 查询关联的实体对象
	 */
	protected T getByUserId(@Nonnull Long userId) {
		Assert.notNull(userId);
		QueryWrapper<T> wrapper = new QueryWrapper<T>()
				.eq("user_id", userId);
		return baseDao.selectOne(wrapper);
	}

	/**
	 * 获取指定实体类的所有实体对象的集合，并以指定的方式进行排序
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public List<T> findAll() {
		return baseDao.selectList(null);
	}

	/**
	 * 添加指定的对象(到对应的数据库表中)
	 */
	public void save(T entity) {
		baseDao.insert(entity);
	}

	public boolean saveBatch(Collection<T> list) {
		return saveBatch(list, IService.DEFAULT_BATCH_SIZE);
	}

	public boolean saveBatch(Collection<T> list, int batchSize) {
		String sqlStatement = SqlHelper.getSqlStatement(mapperClass, SqlMethod.INSERT_ONE);
		return SqlHelper.executeBatch(getSqlSessionFactory(), log, list, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
	}

	/**
	 * 更新指定的对象
	 */
	@Override
	public int update(T entity) {
		return baseDao.updateById(entity);
	}

	/**
	 * 更新指定的对象
	 */
	@Override
	public int update(T entity, Wrapper<T> updateWrapper) {
		return baseDao.update(entity, updateWrapper);
	}

	public boolean updateBatchById(Collection<T> list) {
		return updateBatchById(list, IService.DEFAULT_BATCH_SIZE);
	}

	public boolean updateBatchById(Collection<T> list, int batchSize) {
		String sqlStatement = SqlHelper.getSqlStatement(mapperClass, SqlMethod.UPDATE_BY_ID);
		return SqlHelper.executeBatch(getSqlSessionFactory(), log, list, batchSize, (sqlSession, entity) -> {
			MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
			param.put(Constants.ENTITY, entity);
			sqlSession.update(sqlStatement, param);
		});
	}

	public boolean updateBatchByWrappers(Collection<UpdateWrapper<T>> wrappers) {
		return updateBatchByWrappers(wrappers, IService.DEFAULT_BATCH_SIZE);
	}

	public boolean updateBatchByWrappers(Collection<UpdateWrapper<T>> wrappers, int batchSize) {
		final String sqlStatement = SqlHelper.getSqlStatement(mapperClass, SqlMethod.UPDATE);
		return SqlHelper.executeBatch(getSqlSessionFactory(), log, wrappers, batchSize,
				(sqlSession, wrapper) -> sqlSession.update(sqlStatement, Map.of(Constants.WRAPPER, wrapper)));
	}

	/**
	 * 添加、更新指定的对象
	 */
	protected void saveOrUpdate(T entity, boolean hasId) {
		if (hasId) {
			update(entity);
			return;
		}
		save(entity);
	}

	/**
	 * 添加、更新指定的对象【设置空值使用】
	 *
	 * @param entity 实体对象
	 * @param hasId 是否编辑
	 * @param fieldAndValues 修改的属性【存在相同属性，以该属性为主】
	 */
	protected void saveOrUpdate(T entity, boolean hasId, Object... fieldAndValues) {
		if (hasId) {
			UpdateWrapper<T> wrapper = new UpdateWrapper<>();
			wrapper.eq("id", entity.getId());
			if (X.isValid(fieldAndValues)) {
				for (int i = 0; i < fieldAndValues.length; i++) {
					wrapper.set((String) fieldAndValues[i++], fieldAndValues[i]);
				}
			}
			update(entity, wrapper);
			return;
		}
		save(entity);
	}

	/**
	 * 根据条件查询对应的实体数据
	 */
	protected T selectOne(Wrapper<T> queryWrapper) {
		return baseDao.selectOne(queryWrapper);
	}

	/**
	 * 根据条件查询对应的实体数
	 */
	protected Long count(Wrapper<T> queryWrapper) {
		return baseDao.selectCount(queryWrapper);
	}

	/**
	 * 指示是否存在符合条件的记录
	 */
	protected boolean exists(QueryWrapper<T> queryWrapper) {
		queryWrapper
				.select("1")
				.last("LIMIT 1");
		return !baseDao.selectObjs(queryWrapper).isEmpty();
	}

	/**
	 * 根据条件查询对应的实体数据
	 */
	protected List<T> selectList(Wrapper<T> queryWrapper) {
		return baseDao.selectList(queryWrapper);
	}

	/**
	 * 根据条件查询对应的实体数据并分页
	 */
	protected Page<T> selectPage(Page<?> page, Wrapper<T> queryWrapper) {
		if (page.getSize() < 0) {
			List<T> list = baseDao.selectList(queryWrapper);
			int size = X.size(list);
			return new Page<T>(1, size, size).setList(list);
		}
		return baseDao.selectPage(X.castType(page), queryWrapper);
	}

	/**
	 * 从数据存储中移除指定的实体对象
	 */
	public boolean delete(T entity) {
		Assert.notNull(Bean.idOf(entity));
		return deleteById(entity.getId());
	}

	/**
	 * 从数据存储中移除指定的实体对象
	 */
	public boolean deleteById(K id) {
		return baseDao.deleteById(id) > 0;
	}

	/**
	 * 删除符合 指定的 主键ID 和 商户ID 的实体数据
	 *
	 * @throws PermissionException 如果删除失败，则抛出权限异常
	 */
	public void deleteByIdAndMid(K id, Long merchantId) throws PermissionException {
		UpdateWrapper<T> wrapper = new UpdateWrapper<T>()
				.eq(BaseEntity.ID, id)
				.eq(WithMerchant.MERCHANT_ID, merchantId);
		boolean success = baseDao.delete(wrapper) > 0;
		if (!success) {
			throw new PermissionException();
		}
	}

	/**
	 * 从数据存储中批量移除指定的实体对象
	 */
	public int deleteByIds(K... ids) {
		return deleteByIds(Arrays.asList(ids));
	}

	/**
	 * 从数据存储中批量移除指定的实体对象
	 */
	public int deleteByIds(Collection<K> ids) {
		return baseDao.deleteBatchIds(ids);
	}

	/**
	 * 从数据存储中移除符合指定条件的数据
	 */
	protected int delete(UpdateWrapper<T> wrapper) {
		return baseDao.delete(wrapper);
	}

	/**
	 * 更新符合指定条件的数据
	 */
	protected int update(UpdateWrapper<T> wrapper) {
		Assert.notNull(wrapper);
		return baseDao.update(null, wrapper);
	}

	/**
	 * 更新符合指定条件的数据
	 */
	protected int update(T toUpdate, UpdateWrapper<T> wrapper) {
		Assert.isTrue(toUpdate != null || wrapper != null);
		return baseDao.update(toUpdate, wrapper);
	}

	/**
	 * 判断对象表中是否存在指定属性的值
	 *
	 * @param fieldAndValues 键值对(键必须为字符串)
	 * @since 1.0
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean exists(Object... fieldAndValues) {
		QueryWrapper<T> wrapper = new QueryWrapper<>();
		for (int i = 0; i < fieldAndValues.length; i++) {
			wrapper.eq((String) fieldAndValues[i++], fieldAndValues[i]);
		}
		return exists(wrapper);
	}

	/**
	 * 乐观锁更新业务状态
	 */
	public int updateStatus(Integer newStatus, Integer oldStatus, Long id, @Nullable Date now) {
		UpdateWrapper<T> wrapper = new UpdateWrapper<T>()
				.set(BizEntity.STATUS, newStatus)
				.set(now != null, BizEntity.UPDATE_TIME, now)

				.eq(BizEntity.ID, id)
				.eq(BizEntity.STATUS, oldStatus);

		return baseDao.update(null, wrapper);
	}

	protected static <V> V value(@Nullable ValueEnum<?, V> t) {
		return t == null ? null : t.getValue();
	}

	protected static <T> void incrSetIfHasValue(UpdateWrapper<T> wrapper, @Nonnull String column, @Nullable Number value) {
		if (value == null) {
			return;
		}
		// a = a + {0} 使 MySQL 缓存最近最常用的SQL
		wrapper.setSql(true, column + "=" + column + "+ {0}", value);
	}

}
