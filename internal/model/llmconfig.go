package model

import "time"

type LLMConfig struct {
	ID         int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	CreateTime time.Time `gorm:"column:create_time;autoCreateTime" json:"create_time"`
	BaseURL    string    `gorm:"column:base_url" json:"base_url"`
	APIKey     string    `gorm:"column:api_key" json:"api_key"`
	Model      string    `gorm:"column:model" json:"model"`
	ExtraBody  string    `gorm:"column:extra_body;default:'{}'" json:"extra_body"`
	Enabled    bool      `gorm:"column:enabled;default:false" json:"enabled"`
}

func (LLMConfig) TableName() string { return "llm_configs" }
