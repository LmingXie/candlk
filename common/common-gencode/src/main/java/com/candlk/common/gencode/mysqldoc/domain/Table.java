package com.candlk.common.gencode.mysqldoc.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Table {

	private String name;
	private String comment;

	private List<TableField> fields;

}
