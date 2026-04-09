package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 心理测评量表定义实体
 *
 * 用于配置一整套测评问卷的基础信息和评分维度配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_scale")
@Schema(description = "心理测评量表定义实体")
public class AssessmentScale {

    @TableId(type = IdType.AUTO)
    @Schema(description = "量表ID")
    private Long id;

    @Schema(description = "量表编码（如 PHQ9、GAD7）")
    @NotBlank(message = "量表编码不能为空")
    @Size(max = 50, message = "量表编码长度不能超过50个字符")
    @TableField("code")
    private String code;

    @Schema(description = "量表名称")
    @NotBlank(message = "量表名称不能为空")
    @Size(max = 200, message = "量表名称长度不能超过200个字符")
    @TableField("name")
    private String name;

    @Schema(description = "量表简介/适用场景")
    @TableField("description")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "题目数量")
    @TableField("question_count")
    private Integer questionCount;

    @Schema(description = "维度与分档配置（JSON）")
    @TableField("dimension_config")
    private String dimensionConfig;

    @Schema(description = "创建人ID")
    @TableField("created_by")
    private Long createdBy;

    @Schema(description = "更新人ID")
    @TableField("updated_by")
    private Long updatedBy;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}

