package model

import "time"

type LLMConfig struct {
	ID         int64     `gorm:"column:id;primaryKey;autoIncrement"`
	CreateTime time.Time `gorm:"column:create_time;autoCreateTime"`
	BaseURL    string    `gorm:"column:base_url"`
	APIKey     string    `gorm:"column:api_key"`
	Model      string    `gorm:"column:model"`
	ExtraBody  string    `gorm:"column:extra_body;default:'{}'"`
	Enabled    bool      `gorm:"column:enabled;default:false"`
}

func (LLMConfig) TableName() string { return "llm_configs" }
