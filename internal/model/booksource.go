package model

// BookSource mirrors the Legado-compatible book_sources table with camelCase columns.
type BookSource struct {
	ID             int64  `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	BookSourceName string `gorm:"column:bookSourceName;not null" json:"book_source_name"`
	BookSourceURL  string `gorm:"column:bookSourceUrl;unique" json:"book_source_url"`

	BookNameRule       string `gorm:"column:bookName" json:"book_name_rule"`
	ChapterNameRule    string `gorm:"column:chapterName" json:"chapter_name_rule"`
	ContentRule        string `gorm:"column:content" json:"content_rule"`
	NextContentURLRule string `gorm:"column:nextContentUrl" json:"next_content_url_rule"`

	Enabled bool `gorm:"column:enabled;default:true" json:"enabled"`
}

func (BookSource) TableName() string { return "book_sources" }
