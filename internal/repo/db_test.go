package repo

import "testing"

func TestSQLiteDSN(t *testing.T) {
	tests := []struct {
		name string
		dsn  string
		want string
	}{
		{name: "memory", dsn: "", want: ":memory:"},
		{name: "relative", dsn: "data/novel.db", want: "data/novel.db"},
		{name: "absolute windows", dsn: "/D:/repo/novel.db", want: "D:/repo/novel.db"},
		{name: "absolute unix", dsn: "/tmp/novel.db", want: "/tmp/novel.db"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := sqliteDSN(tt.dsn); got != tt.want {
				t.Fatalf("sqliteDSN(%q) = %q, want %q", tt.dsn, got, tt.want)
			}
		})
	}
}

func TestDialectorForURI(t *testing.T) {
	tests := []struct {
		name    string
		uri     string
		wantErr bool
	}{
		{name: "postgres", uri: "postgres://user:pass@localhost/db", wantErr: false},
		{name: "mysql", uri: "mysql://user:pass@/db?parseTime=True", wantErr: false},
		{name: "sqlite", uri: "sqlite:///tmp/novel.db", wantErr: false},
		{name: "bad", uri: "redis://localhost", wantErr: true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			_, err := dialectorForURI(tt.uri)
			if tt.wantErr && err == nil {
				t.Fatalf("dialectorForURI(%q) expected error", tt.uri)
			}
			if !tt.wantErr && err != nil {
				t.Fatalf("dialectorForURI(%q) unexpected error: %v", tt.uri, err)
			}
		})
	}
}
