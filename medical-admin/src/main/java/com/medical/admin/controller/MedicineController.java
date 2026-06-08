package com.medical.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.base.BaseController;
import com.medical.common.result.PageResult;
import com.medical.common.result.R;
import com.medical.model.dto.MedicineQueryDTO;
import com.medical.model.entity.Medicine;
import com.medical.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 药品管理控制器
 */
@RestController
@RequestMapping("/api/medicine")
@Tag(name = "药品管理", description = "中药药品的增删改查接口")
public class MedicineController extends BaseController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping("/page")
    @Operation(summary = "分页查询药品列表")
    public R<PageResult<Medicine>> page(@Valid MedicineQueryDTO queryDTO) {
        Page<Medicine> page = medicineService.pageQuery(queryDTO);
        return R.ok(PageResult.of(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询药品详情")
    public R<Medicine> getById(@PathVariable Long id) {
        Medicine medicine = medicineService.getById(id);
        if (medicine == null) {
            return R.fail(404, "药品不存在");
        }
        return R.ok(medicine);
    }

    @PostMapping
    @Operation(summary = "新增药品")
    @PreAuthorize("hasRole('admin')")
    public R<Void> add(@Valid @RequestBody Medicine medicine) {
        medicineService.addMedicine(medicine);
        return R.ok("新增成功");
    }

    @PutMapping
    @Operation(summary = "更新药品信息")
    @PreAuthorize("hasRole('admin')")
    public R<Void> update(@Valid @RequestBody Medicine medicine) {
        medicineService.updateMedicine(medicine);
        return R.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除药品")
    @PreAuthorize("hasRole('admin')")
    public R<Void> delete(@PathVariable Long id) {
        medicineService.removeById(id);
        return R.ok("删除成功");
    }

    @PutMapping("/stock/{id}")
    @Operation(summary = "更新药品库存")
    @Parameter(name = "quantity", description = "变更数量（正数入仓，负数出仓）")
    @PreAuthorize("hasRole('admin')")
    public R<Void> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        medicineService.updateStock(id, quantity);
        return R.ok("库存更新成功");
    }

    @GetMapping("/alert")
    @Operation(summary = "查询库存预警药品列表")
    public R<PageResult<Medicine>> stockAlert(MedicineQueryDTO queryDTO) {
        queryDTO.setStockAlert(true);
        Page<Medicine> page = medicineService.pageQuery(queryDTO);
        return R.ok(PageResult.of(page));
    }

    @GetMapping("/stats/category")
    @Operation(summary = "统计各分类药品数量（工作台饼图数据）")
    public R<java.util.List<java.util.Map<String, Object>>> countByCategory() {
        return R.ok(medicineService.countByCategory());
    }
}
