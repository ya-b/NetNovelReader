package webdriver

// New returns a Driver chosen by the CHROME_DRIVER config value.
// "none" uses plain HTTP; "zendriver"/"selenium"/"cdp"/"chrome" all drive a
// real Chrome instance over the Chrome DevTools Protocol via chromedp.
func New(name string) Driver {
	switch name {
	case "none":
		return NewNoneDriver()
	case "zendriver", "selenium", "cdp", "chrome":
		return NewChromeDriver()
	default:
		return NewNoneDriver()
	}
}
