package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 心理测评题目实体
 *
 * 支持单选、多选、量表等类型，选项及计分规则采用JSON配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_question")
@Schema(description = "心理测评题目实体")
public class AssessmentQuestion {

    @TableId(type = IdType.AUTO)
    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "所属量表ID")
    @NotNull(message = "量表ID不能为空")
    @TableField("scale_id")
    private Long scaleId;

    @Schema(description = "题目序号（从1开始）")
    @NotNull(message = "题目序号不能为空")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "题干内容")
    @NotBlank(message = "题干内容不能为空")
    @TableField("content")
    private String content;

    @Schema(description = "题型：SINGLE_CHOICE、MULTI_CHOICE、SCALE 等")
    @NotBlank(message = "题型不能为空")
    @Size(max = 50, message = "题型长度不能超过50个字符")
    @TableField("type")
    private String type;

    @Schema(description = "选项配置（JSON）")
    @TableField("options_json")
    private String optionsJson;

    @Schema(description = "计分规则配置（JSON）")
    @TableField("scoring_json")
    private String scoringJson;

    @Schema(description = "是否必答：0-否，1-是")
    @TableField("required")
    private Integer required;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否为必答题
     */
    public boolean isRequired() {
        return required != null && required == 1;
    }
}

