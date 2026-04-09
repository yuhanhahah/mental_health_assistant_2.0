package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户心理测评记录实体
 *
 * 记录一次完整的量表测评结果，可选关联AI分析任务与AI解读结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_record")
@Schema(description = "用户心理测评记录实体")
public class AssessmentRecord {

    @TableId(type = IdType.AUTO)
    @Schema(description = "测评记录ID")
    private Long id;

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "量表ID")
    @NotNull(message = "量表ID不能为空")
    @TableField("scale_id")
    private Long scaleId;

    @Schema(description = "总分")
    @TableField("total_score")
    private Integer totalScore;

    @Schema(description = "各维度得分（JSON）")
    @TableField("dimension_scores_json")
    private String dimensionScoresJson;

    @Schema(description = "结果等级（如 正常/轻度/中度/重度）")
    @TableField("level")
    private String level;

    @Schema(description = "系统内置建议文案")
    @TableField("suggestion")
    private String suggestion;

    @Schema(description = "AI个性化解读结果（JSON）")
    @TableField("ai_analysis_json")
    private String aiAnalysisJson;

    @Schema(description = "关联的AI分析任务ID")
    @TableField("ai_task_id")
    private Long aiTaskId;

    @Schema(description = "测评时间")
    @TableField("assessed_at")
    private LocalDateTime assessedAt;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否已包含AI解读结果
     */
    public boolean hasAiAnalysis() {
        return aiAnalysisJson != null && !aiAnalysisJson.trim().isEmpty();
    }
}

