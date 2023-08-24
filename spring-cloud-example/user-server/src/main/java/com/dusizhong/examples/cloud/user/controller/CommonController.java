package com.dusizhong.examples.cloud.user.controller;

import com.dusizhong.examples.cloud.user.entity.CommArea;
import com.dusizhong.examples.cloud.user.model.BaseResp;
import com.dusizhong.examples.cloud.user.model.BaseTree;
import com.dusizhong.examples.cloud.user.repository.CommAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private CommAreaRepository commAreaRepository;

    @PostMapping("/area/list")
    public BaseResp areaList(@RequestBody CommArea post) {
        Example<CommArea> example = Example.of(post);
        return BaseResp.success(commAreaRepository.findAll(example, new Sort(Sort.Direction.ASC, "sortId")));
    }

    @Cacheable(value = "resultTree")
    @PostMapping("/area/tree")
    public BaseResp areaTree(@RequestBody CommArea post) {
        List<CommArea> commAreaList = commAreaRepository.findAll(new Sort(Sort.Direction.ASC, "sortId"));
        List<CommArea> provinceList = commAreaList.stream().filter(a -> a.getAreaType().equals("0")).collect(Collectors.toList());
        if(!StringUtils.isEmpty(post.getAreaCode())) {
            provinceList = provinceList.stream().filter(p -> p.getAreaCode().equals(post.getAreaCode())).collect(Collectors.toList());
        }
        List<CommArea> cityList = commAreaList.stream().filter(a -> a.getAreaType().equals("1")).collect(Collectors.toList());
        List<CommArea> areaList = commAreaList.stream().filter(a -> a.getAreaType().equals("2")).collect(Collectors.toList());
        List<BaseTree> resultTree = new ArrayList<>();
        for(CommArea province : provinceList) {
            BaseTree provinceTree = new BaseTree();
            provinceTree.setCode(province.getAreaCode());
            provinceTree.setName(province.getAreaName());
            List<BaseTree> provinceChildren = new ArrayList<>();
            for(CommArea city : cityList) {
                if(city.getAreaParent().equals(province.getAreaCode())) {
                    BaseTree cityTree = new BaseTree();
                    cityTree.setCode(city.getAreaCode());
                    cityTree.setName(city.getAreaName());
                    List<BaseTree> cityChildren = new ArrayList<>();
                    for(CommArea area : areaList) {
                        if(area.getAreaParent().equals(city.getAreaCode())) {
                            BaseTree areaTree = new BaseTree();
                            areaTree.setCode(area.getAreaCode());
                            areaTree.setName(area.getAreaName());
                            areaTree.setChildren(null);
                            cityChildren.add(areaTree);
                        }
                    }
                    cityTree.setChildren(cityChildren);
                    provinceChildren.add(cityTree);
                }
            }
            provinceTree.setChildren(provinceChildren);
            resultTree.add(provinceTree);
        }
        return BaseResp.success(resultTree);
    }

    @CacheEvict(value="resultTree", allEntries=true)
    @PostMapping("/area/refresh")
    public BaseResp refresh() {
        return BaseResp.success();
    }
}
