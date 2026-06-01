package com.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.model.entity.Medicine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 药品Mapper
 */
@Mapper
public interface MedicineMapper extends BaseMapper<Medicine> {

    /**
     * 多条件复杂查询（支持关联分类表）
     */
    List<Medicine> selectByMultiCondition(@Param("query") Medicine query);

    /**
     * 批量更新库存
     */
    int batchUpdateStock(@Param("ids") List<Long> ids, @Param("quantity") int quantity);

    /**
     * 统计各分类药品数量
     */
    List<Map<String, Object>> countByCategory();
}
