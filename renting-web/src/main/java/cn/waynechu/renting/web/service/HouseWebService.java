package cn.waynechu.renting.web.service;

import cn.waynechu.common.util.CollectionUtil;
import cn.waynechu.renting.facade.dto.HouseDTO;
import cn.waynechu.renting.facade.service.HouseService;
import cn.waynechu.renting.facade.vo.HouseVO;
import cn.waynechu.renting.web.convert.HouseConvert;
import cn.waynechu.webcommon.page.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhuwei
 * @date 2018/12/29 9:53
 */
@Service
public class HouseWebService {

    @Autowired
    private HouseService houseService;

    public HouseVO getById(Long id) {
        HouseVO returnValue = null;

        HouseDTO houseDTO = houseService.getById(id);
        if (houseDTO != null) {
            returnValue = HouseConvert.convertHouseVO(houseDTO);
        }
        return returnValue;
    }

    public boolean create(HouseDTO houseDTO) {
        return houseService.create(houseDTO);
    }

    public boolean update(HouseDTO houseDTO) {
        return houseService.update(houseDTO);
    }

    public boolean removeById(Long id) {
        return houseService.removeById(id);
    }

    public PageInfo<HouseVO> search(HouseDTO houseDTO, Integer pageNum, Integer pageSize) {
        PageInfo<HouseVO> returnValue = null;

        PageInfo<HouseDTO> search = houseService.search(houseDTO, pageNum, pageSize);
        List<HouseDTO> list = search.getList();
        if ( CollectionUtil.isNotNullOrEmpty(list)) {
            List<HouseVO> houseVOS = HouseConvert.convertHouseVOS(list);
            returnValue = PageInfo.of(houseVOS);
        }
        return returnValue;
    }

    public boolean copyByIdTransition(Long id) {
        return houseService.copyByIdTransition(id);
    }
}
