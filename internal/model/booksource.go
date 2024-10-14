package model

// BookSource mirrors the Legado-compatible book_sources table with camelCase columns.
type BookSource struct {
	ID             int64  `gorm:"column:id;primaryKey;autoIncrement"`
	BookSourceName string `gorm:"column:bookSourceName;not null"`
	BookSourceURL  string `gorm:"column:bookSourceUrl;unique"`

	BookNameRule       string `gorm:"column:bookName"`
	ChapterNameRule    string `gorm:"column:chapterName"`
	ContentRule        string `gorm:"column:content"`
	NextContentURLRule string `gorm:"column:nextContentUrl"`

	Enabled bool `gorm:"column:enabled;default:true"`
}

func (BookSource) TableName() string { return "book_sources" }
