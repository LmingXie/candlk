package com.candlk.common.gencode.mysqldoc.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {

	private List<List<TableField>> fieldsList;
	private List<Table> tables;

}
