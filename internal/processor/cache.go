package processor

import (
	"container/list"
	"sync"
)

type cacheItem struct {
	key   string
	value string
}

type lruCache struct {
	mu       sync.Mutex
	capacity int
	list     *list.List
	items    map[string]*list.Element
}

func newLRUCache(capacity int) *lruCache {
	return &lruCache{
		capacity: capacity,
		list:     list.New(),
		items:    make(map[string]*list.Element),
	}
}

func (c *lruCache) Get(key string) (string, bool) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if elem, ok := c.items[key]; ok {
		c.list.MoveToFront(elem)
		return elem.Value.(*cacheItem).value, true
	}
	return "", false
}

func (c *lruCache) Add(key, value string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if elem, ok := c.items[key]; ok {
		c.list.MoveToFront(elem)
		elem.Value.(*cacheItem).value = value
		return
	}
	elem := c.list.PushFront(&cacheItem{key: key, value: value})
	c.items[key] = elem
	if c.list.Len() > c.capacity {
		c.removeOldest()
	}
}

func (c *lruCache) removeOldest() {
	elem := c.list.Back()
	if elem != nil {
		c.list.Remove(elem)
		item := elem.Value.(*cacheItem)
		delete(c.items, item.key)
	}
}
