package ${package.ServiceImpl};

import org.springframework.stereotype.Service;

/**
 * ${table.comment} 服务实现类
 *
 * @author ${author}
 * @since ${date}
 */
@Service
<#if kotlin>
open class ${table.serviceImplName} : ${superServiceImplClass}<${entity}, ${table.mapperName}, Long>(), ${table.serviceName} {

}
<#else>
public class ${table.serviceImplName} extends ${superServiceImplClass}<${entity}, ${table.mapperName}, Long> {

}
</#if>
