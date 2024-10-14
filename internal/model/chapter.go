package model

// ChapterContent represents a parsed chapter.
type ChapterContent struct {
	BookName    string `json:"book_name"`
	ChapterName string `json:"chapter_name"`
	ChapterURL  string `json:"chapter_url"`
	Content     string `json:"content"`
	NextURL     string `json:"next_url"`
}
