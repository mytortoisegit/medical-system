package com.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.model.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方Mapper
 */
@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
}
