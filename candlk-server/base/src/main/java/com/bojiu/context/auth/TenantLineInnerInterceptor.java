package com.bojiu.context.auth;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BaseMultiTableInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.toolkit.PropertyMapper;
import com.bojiu.context.model.*;
import com.bojiu.context.web.RequestContextImpl;
import lombok.*;
import me.codeplayer.util.X;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * @see com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantLineInnerInterceptor extends BaseMultiTableInnerInterceptor implements InnerInterceptor {

	private TenantLineHandler tenantLineHandler;
	// static final Pattern merchantIdWhere = Pattern.compile("WHERE\\s+[\\s\\S]*merchant_id\\s+(=|IN)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	// beforeQuery -> processSelect -> beforePrepare
	@Override
	public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
		if (InterceptorIgnoreHelper.willIgnoreTenantLine(ms.getId())) {
			return;
		}
		PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
		String sql = mpBs.sql();
		int pos = StringUtils.lastIndexOf(sql, WithMerchant.MERCHANT_ID);
		if (pos > 0 && StringUtils.lastIndexOfIgnoreCase(sql, "WHERE", pos - 1) > 0) {
			if (!MemberType.fromBackstage()) {
				return;
			}
			Member user = RequestContextImpl.get().sessionUser();
			if (user == null || user.type() != MemberType.AGENT) {
				return;
			}
		}
		sql = parserSingle(sql, null);
		mpBs.sql(sql);
	}

	@Override
	protected void processSelect(Select select, int index, String sql, Object obj) {
		final String whereSegment = (String) obj;
		processSelectBody(select, whereSegment);
		List<WithItem> withItemsList = select.getWithItemsList();
		if (!CollectionUtils.isEmpty(withItemsList)) {
			for (WithItem withItem : withItemsList) {
				processSelectBody(withItem, whereSegment);
			}
		}
	}

	@Override
	public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
		PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
		MappedStatement ms = mpSh.mappedStatement();
		SqlCommandType sct = ms.getSqlCommandType();
		if (sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
			if (InterceptorIgnoreHelper.willIgnoreTenantLine(ms.getId())) {
				return;
			}
			PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
			mpBs.sql(parserMulti(mpBs.sql(), null));
		}
	}

	@Override
	protected void processInsert(Insert insert, int index, String sql, Object obj) {
	}

	/**
	 * update 语句处理
	 */
	@Override
	protected void processUpdate(Update update, int index, String sql, Object obj) {
		final Table table = update.getTable();
		if (tenantLineHandler.ignoreTable(table.getName())) {
			// 过滤退出执行
			return;
		}
		List<UpdateSet> sets = update.getUpdateSets();
		final String clause = (String) obj;
		if (!CollectionUtils.isEmpty(sets)) {
			for (UpdateSet us : sets) {
				for (Expression ex : us.getValues()) {
					if (ex instanceof Select s) {
						processSelectBody(s, clause);
					}
				}
			}
		}
		update.setWhere(this.andExpression(table, update.getWhere(), clause));
	}

	/**
	 * delete 语句处理
	 */
	@Override
	protected void processDelete(Delete delete, int index, String sql, Object obj) {
		if (tenantLineHandler.ignoreTable(delete.getTable().getName())) {
			// 过滤退出执行
			return;
		}
		delete.setWhere(this.andExpression(delete.getTable(), delete.getWhere(), (String) obj));
	}

	/**
	 * 租户字段别名设置
	 * <p>tenantId 或 tableAlias.tenantId</p>
	 *
	 * @param table 表对象
	 * @return 字段
	 */
	protected Column getAliasColumn(Table table) {
		// TODO 该起别名就要起别名，禁止修改此处逻辑
		final Alias alias = table.getAlias();
		String field = tenantLineHandler.getTenantIdColumn();
		if (alias != null) {
			field = alias.getName() + StringPool.DOT + field;
		}
		return new Column(field);
	}

	/**
	 * 单独定制处理代理字段
	 */

	protected Column getAgentAliasColumn(Table table) {
		TableInfo tableInfo = TableInfoHelper.getTableInfo(table.getName());
		if (tableInfo != null && IgnoreTopAgent.class.isAssignableFrom(tableInfo.getEntityType())) {
			return null;
		}
		final Alias alias = table.getAlias();
		String field = "gs_user".equals(table.getName()) ? "top_agent_user_id" : "top_user_id";
		if (alias != null) {
			field = alias.getName() + StringPool.DOT + field;
		}
		return new Column(field);
	}

	@Override
	public void setProperties(Properties properties) {
		PropertyMapper.newInstance(properties).whenNotBlank("tenantLineHandler",
				ClassUtils::newInstance, this::setTenantLineHandler);
	}

	/**
	 * 构建租户条件表达式
	 *
	 * @param table 表对象
	 * @param where 当前where条件
	 * @param whereSegment 所属Mapper对象全路径（在原租户拦截器功能中，这个参数并不需要参与相关判断）
	 * @return 租户条件表达式
	 * @see BaseMultiTableInnerInterceptor#buildTableExpression(Table, Expression, String)
	 */
	@Override
	public Expression buildTableExpression(final Table table, final Expression where, final String whereSegment) {
		if (tenantLineHandler.ignoreTable(table.getName())) {
			return null;
		}
		final Expression exp = tenantLineHandler.getTenantId();
		final Column col = getAliasColumn(table);
		if (exp instanceof InExpression inExp) {
			inExp.setLeftExpression(col);
			return inExp;
		} else if (exp instanceof AndExpression addExp) {
			EqualsTo eqExp = X.castType(addExp.getRightExpression());
			eqExp.setLeftExpression(getAgentAliasColumn(table));
			if (where != null && StringUtils.lastIndexOf(where.toString(), WithMerchant.MERCHANT_ID) > 0) {
				return eqExp.getLeftExpression() == null ? null : eqExp;
			}
			Expression leftExp = addExp.getLeftExpression();
			if (leftExp instanceof InExpression inExp) {
				inExp.setLeftExpression(col);
			} else if (leftExp instanceof EqualsTo inExp) {
				inExp.setLeftExpression(col);
			}
			return eqExp.getLeftExpression() == null ? leftExp : addExp;
		}
		return new EqualsTo(col, exp);
	}

}
