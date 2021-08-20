function offsetAnchor() {
    console.log("OffsetAnchor Ran !!!");
    if(location.hash.length != 0) {
        window.scrollTo(window.scrollX, window.scrollY - 100);
        console.log("OffsetAnchor Scrolled !!!");
    }
}

// This will capture hash changes while on the page
window.addEventListener("hashchange", offsetAnchor);

// This is here so that when you enter the page with a hash,
// it can provide the offset in that case too. Having a timeout
// seems necessary to allow the browser to jump to the anchor first.
window.onload = offsetAnchor;