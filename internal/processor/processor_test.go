package processor

import (
	"context"
	"fmt"
	"sync"
	"testing"
)

type mockDriver struct {
	getCalls  int
	srcCalls  int
	lastURL   string
	pageHTML  string
	mu        sync.Mutex
}

func (m *mockDriver) Start(ctx context.Context) error { return nil }
func (m *mockDriver) Stop(ctx context.Context) error  { return nil }
func (m *mockDriver) Get(ctx context.Context, url string) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.getCalls++
	m.lastURL = url
	return nil
}
func (m *mockDriver) CurrentURL(ctx context.Context) (string, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	return m.lastURL, nil
}
func (m *mockDriver) PageSource(ctx context.Context) (string, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.srcCalls++
	return m.pageHTML, nil
}
func (m *mockDriver) Title(ctx context.Context) (string, error) { return "", nil }
func (m *mockDriver) ExecuteScript(ctx context.Context, script string) (string, error) {
	return "", nil
}

func TestLRUCacheBasic(t *testing.T) {
	cache := newLRUCache(2)

	cache.Add("key1", "val1")
	cache.Add("key2", "val2")

	// Verify both exist
	if v, ok := cache.Get("key1"); !ok || v != "val1" {
		t.Errorf("expected key1 to be val1, got %v", v)
	}
	if v, ok := cache.Get("key2"); !ok || v != "val2" {
		t.Errorf("expected key2 to be val2, got %v", v)
	}

	// Add key3, should evict key1 since key2 was accessed after key1
	cache.Add("key3", "val3")

	if _, ok := cache.Get("key1"); ok {
		t.Error("expected key1 to be evicted")
	}
	if v, ok := cache.Get("key2"); !ok || v != "val2" {
		t.Errorf("expected key2 to be val2, got %v", v)
	}
	if v, ok := cache.Get("key3"); !ok || v != "val3" {
		t.Errorf("expected key3 to be val3, got %v", v)
	}
}

func TestProcessorLoadURLCaching(t *testing.T) {
	mock := &mockDriver{pageHTML: "<html>test</html>"}
	proc := &Processor{
		Driver: mock,
		cache:  newLRUCache(3),
	}

	ctx := context.Background()

	// First load: cache miss
	html1, err := proc.loadURL(ctx, "http://example.com/1")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if html1 != "<html>test</html>" {
		t.Errorf("expected '<html>test</html>', got %q", html1)
	}

	// Second load of same URL: cache hit
	html2, err := proc.loadURL(ctx, "http://example.com/1")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if html2 != "<html>test</html>" {
		t.Errorf("expected '<html>test</html>', got %q", html2)
	}

	// Verify driver was only called once
	mock.mu.Lock()
	getCalls := mock.getCalls
	srcCalls := mock.srcCalls
	mock.mu.Unlock()

	if getCalls != 1 {
		t.Errorf("expected 1 Get call, got %d", getCalls)
	}
	if srcCalls != 1 {
		t.Errorf("expected 1 PageSource call, got %d", srcCalls)
	}

	// Load other URLs to test LRU eviction
	proc.loadURL(ctx, "http://example.com/2") // cache size 2: [2, 1]
	proc.loadURL(ctx, "http://example.com/3") // cache size 3: [3, 2, 1]
	proc.loadURL(ctx, "http://example.com/4") // cache size 3: [4, 3, 2], 1 is evicted

	// Loading 1 again should be a cache miss
	mock.mu.Lock()
	mock.getCalls = 0
	mock.srcCalls = 0
	mock.mu.Unlock()

	_, err = proc.loadURL(ctx, "http://example.com/1")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	mock.mu.Lock()
	getCalls = mock.getCalls
	mock.mu.Unlock()

	if getCalls != 1 {
		t.Errorf("expected Get call due to eviction, got %d", getCalls)
	}
}

func TestLRUCacheConcurrency(t *testing.T) {
	cache := newLRUCache(10)
	var wg sync.WaitGroup
	for i := 0; i < 100; i++ {
		wg.Add(1)
		go func(val int) {
			defer wg.Done()
			key := fmt.Sprintf("key-%d", val%15)
			cache.Add(key, fmt.Sprintf("val-%d", val))
			cache.Get(key)
		}(i)
	}
	wg.Wait()
}
