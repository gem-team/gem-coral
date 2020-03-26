/**
 * 开源版本请务必保留此注释头信息，若删除gemframe官方保留所有法律责任追究！
 * 本软件受国家版权局知识产权以及国家计算机软件著作权保护（登记号：2018SR503328）
 * 不得恶意分享产品源代码、二次转售等，违者必究。
 * Copyright (c) 2020 gemframework all rights reserved.
 * http://www.gemframework.com
 * 版权所有，侵权必究！
 */
package com.gemframework.controller.prekit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gemframework.common.exception.GemException;
import com.gemframework.common.utils.GemBeanUtils;
import com.gemframework.common.utils.GemStringUtils;
import com.gemframework.model.common.BaseEntityVo;
import com.gemframework.model.common.PageInfo;
import com.gemframework.model.common.ZtreeEntity;
import com.gemframework.model.entity.po.Role;
import com.gemframework.model.entity.po.User;
import com.gemframework.model.enums.ErrorCode;
import com.gemframework.model.enums.SortType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jetbrains.annotations.NotNull;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class BaseController {

    @NotNull
    public static Page setOrderPage(PageInfo pageInfo) {
        OrderItem orderItem = new OrderItem();
        orderItem.setColumn("sort_number").setAsc(SortType.asc.getCode() == 0);
        orderItem.setColumn(pageInfo.getSort()).setAsc(pageInfo.getOrder().asc.getCode() == 0);

        List orders = new ArrayList();
        orders.add(orderItem);
        Page page = new Page();
        page.setOrders(orders);
        page.setSize(pageInfo.getLimit());
        page.setCurrent(pageInfo.getPage());
        return page;
    }

    public static QueryWrapper setSort() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.orderByAsc("sort_number");
        return queryWrapper;
    }

    public static QueryWrapper makeQueryMaps(BaseEntityVo vo) {
        QueryWrapper queryWrapper = setSort();
        queryWrapper.eq("deleted",0);
        Map<String,Object> map = GemBeanUtils.ObjectToMap(vo);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fieldName = GemStringUtils.humpToLine(entry.getKey());
            Object paramVal = entry.getValue();
            log.info("key= " + fieldName + " and value= "+paramVal);
            queryWrapper.like(paramVal != null && StringUtils.isNotBlank(String.valueOf(paramVal)),fieldName,paramVal);
        }
        return queryWrapper;
    }


    /**
     * 将list格式是权限数据，转化成tree格式的权限数据。
     * @param treeNodes 传入的树节点列表
     * @return
     */
    public static List<ZtreeEntity> toTree(List<ZtreeEntity> treeNodes) {
        List<ZtreeEntity> trees = new ArrayList<ZtreeEntity>();
        for (ZtreeEntity treeNode : treeNodes) {
            if (-1 == (treeNode.getPid())) {
                trees.add(treeNode);
            }
            for (ZtreeEntity it : treeNodes) {
                if (it.getPid() == treeNode.getId()) {
                    if (treeNode.getChildren() == null) {
                        treeNode.setChildren(new ArrayList<ZtreeEntity>());
                    }
                    treeNode.getChildren().add(it);
                }
            }
        }
        return trees;
    }


    public static List<ZtreeEntity> initRootTree() {
        List<ZtreeEntity> ztreeEntities = new ArrayList<>();
        ZtreeEntity ztreeEntity = ZtreeEntity.builder()
                .id(0L)
                .pid(-1L)
                .name("顶层目录|全选/全消")
                .title("顶层目录|全选/全消")
                .level(0)
                .open(true)
                .nocheck(true)
                .build();
        ztreeEntities.add(ztreeEntity);

        return ztreeEntities;
    }

    public static void GemValidate(Object object, Class<?>... groups){
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            ConstraintViolation<Object> constraint = (ConstraintViolation<Object>)constraintViolations.iterator().next();
            throw new GemException(ErrorCode.PARAM_EXCEPTION.getCode(),constraint.getMessage());
        }
    }

    protected User getUser() {
        return (User) SecurityUtils.getSubject().getSession().getAttribute("user");
    }

    protected Set<String> getRolesFlag() {
        return (Set) SecurityUtils.getSubject().getSession().getAttribute("roleFlags");
    }

    protected Set<Role> getRoles() {
        return (Set) SecurityUtils.getSubject().getSession().getAttribute("roles");
    }

}