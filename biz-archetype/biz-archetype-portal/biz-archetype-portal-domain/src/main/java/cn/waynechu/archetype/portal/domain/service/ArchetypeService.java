package cn.waynechu.archetype.portal.domain.service;

import cn.waynechu.archetype.portal.dal.dataobject.ArchetypeDO;
import cn.waynechu.archetype.portal.facade.request.CreateArchetypeRequest;
import cn.waynechu.archetype.portal.facade.request.SearchArchetypeRequest;
import cn.waynechu.archetype.portal.facade.request.UpdateArchetypeRequest;
import cn.waynechu.archetype.portal.facade.response.SearchArchetypeResponse;
import cn.waynechu.facade.common.page.BizPageInfo;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhuwei
 * @date 2020-06-20 09:31
 */
public interface ArchetypeService {

    /**
     * 查询原型列表
     *
     * @param request req
     * @return 原型列表
     */
    BizPageInfo<SearchArchetypeResponse> search(SearchArchetypeRequest request);

    /**
     * 新增项目原型
     *
     * @param request req
     * @return 原型id
     */
    Long create(CreateArchetypeRequest request);

    /**
     * 下载项目原型
     *
     * @param id       原型id
     * @param response resp
     */
    void download(Long id, HttpServletResponse response);


    /**
     * 删除项目原型
     *
     * @param id 项目原型id
     */
    void remove(Long id);

    /**
     * 更新项目原型
     *
     * @param request req
     * @return 原型id
     */
    Long update(UpdateArchetypeRequest request);

    /**
     * 校验原型是否存在
     *
     * @param id 原型id
     * @return 存在返回原型信息。否则抛出
     */
    ArchetypeDO checkArchetypeExist(Long id);
}




