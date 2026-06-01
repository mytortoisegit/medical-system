package com.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.model.entity.MedicineCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品分类Mapper
 */
@Mapper
public interface MedicineCategoryMapper extends BaseMapper<MedicineCategory> {
}
