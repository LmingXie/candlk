package ${package.Controller};

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>

import io.swagger.annotations.Api;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
* ${table.comment!} 控制器
*
* @author ${author}
* @since ${date}
*/
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName??>${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
	<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
	<#else>
public class ${table.controllerName} {
	</#if>

	@Resource
	${table.serviceName} ${table.serviceName ? uncap_first};

}
</#if>
