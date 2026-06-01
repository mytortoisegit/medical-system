package com.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.model.entity.PrescriptionDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方明细Mapper
 */
@Mapper
public interface PrescriptionDetailMapper extends BaseMapper<PrescriptionDetail> {
}
