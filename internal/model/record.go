package model

import "time"

type Record struct {
	ID          int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	CreateTime  time.Time `gorm:"column:create_time;autoCreateTime" json:"create_time"`
	UpdateTime  time.Time `gorm:"column:update_time;autoUpdateTime" json:"update_time"`
	BookName    string    `gorm:"column:book_name;uniqueIndex" json:"book_name"`
	ChapterName string    `gorm:"column:chapter_name" json:"chapter_name"`
	ChapterURL  string    `gorm:"column:chapter_url" json:"chapter_url"`
}

func (Record) TableName() string { return "records" }
