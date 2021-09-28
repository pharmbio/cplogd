var imgW_scale = 1.25;
var imgH_fraq = .35;

function calc_w() {
    return Math.floor(calc_h() * imgW_scale);
}
function calc_h() {
    return Math.floor(window.innerHeight * imgH_fraq);
}

function do_draw(jsme, imgRef, url) {
    window.fetch ? _redraw_POST(jsme,imgRef,url) : _redraw_GET(jsme,imgRef,url)
}

function _redraw_POST(jsme, imgRef, baseURL){
    // Use the Fetch API to make a proper POST call

    var url = _fix_url(baseURL); 
    var opts = {
        method: 'POST',
        body: jsme.molFile(true),
        mode: 'no-cors',
    }
    console.log("using the URL: " + url)
    // send POST request
    fetch(url, opts)
        .then(res => res.blob())
        .catch(err => _fail_img(imgRef))
        .then(function(imgBlob){
            var objectURL = URL.createObjectURL(imgBlob);
            imgRef.src = objectURL;
            _success_img(imgRef)
        })
        .catch(err => _fail_img(imgRef))
}

function _redraw_GET(jsme, imgRef, baseURL){
    // Draws using the old GET method, but uses the SMILES to not get overflow of URL-length
    console.log('re-draw with GET')
    var imageurl = _fix_url(baseURL) + 
                 + "&molecule=" + encodeURIComponent(jsme.smiles());
    var tester = new Image();
    tester.addEventListener('error', _fail_img(imgRef))
    tester.addEventListener('load', _success_img(imgRef))
    tester.src = imageurl;
    this.imgRef.src = imageurl;
}

function _fix_url(baseURL) {
    var url = baseURL
        + '&imageWidth=' + calc_w()
        + '&imageHeight=' + calc_h()
        + '&addTitle=true';
    return url;
}

function _prepend_amp(url){
    return url.endsWith('&') ? url : 
        (url.endsWith('?') ? url : (url + '&'));
}

function _success_img(imgRef) {
    imgRef.alt = "";
}
function _fail_img(imgRef) {
	 imgRef.src = ""
    imgRef.alt = 'ERROR: Something went wrong with rendering the prediction image. Please try with another molecule or contact a site administrator if the problem persists.';
//     imgRef.style = 'color: red;'
}

