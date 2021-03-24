/*var collapse0 = document.getElementById('collapse0');
var collapse1 = document.getElementById('collapse1');
var collapse2 = document.getElementById('collapse2');

var bsCollapse0 = new bootstrap.Collapse(collapse0, {
  toggle: false
})
var bsCollapse1 = new bootstrap.Collapse(collapse1, {
  toggle: false
})
var bsCollapse2 = new bootstrap.Collapse(collapse2, {
  toggle: false
})*/

var selects = [
document.getElementById('setting-select-0'),
document.getElementById('setting-select-1'),
document.getElementById('setting-select-2'),
document.getElementById('setting-select-3')
];

var formOptions = [
document.getElementById('minify-form-options'),
document.getElementById('beautify-form-options'),
document.getElementById('xml-form-options'),
document.getElementById('base64-form-options')
];

var classes = [];
classes = classes.concat(Array.from(document.getElementsByClassName('minify-form-options')));
classes = classes.concat(Array.from(document.getElementsByClassName('beautify-form-options')));
classes = classes.concat(Array.from(document.getElementsByClassName('xml-form-options')));
classes = classes.concat(Array.from(document.getElementsByClassName('base64-form-options')));

console.log(classes.length);
classes = getUniques(classes);
console.log(classes.length);


function getUniques(array){
    var res = [];
    for (var i = 0, l = array.length; i < l; i++)
        if (res.indexOf(array[i]) === -1 && array[i] !== '')
            res.push(array[i]);
    return res;
}

function showFormOptions(c){
    for(var j = 0; j < classes.length; j++){
        if(classes[j].classList.contains(c)){
            classes[j].style.display = 'block';
        } else {
            classes[j].style.display = 'none';
        }
    }
}

selects[0].onclick = function(){
showFormOptions('minify-form-options');
}
selects[1].onclick = function(){
showFormOptions('beautify-form-options');
}
selects[2].onclick = function(){
showFormOptions('xml-form-options');
}
selects[3].onclick = function(){
showFormOptions('base64-form-options');
}


window.onload = function() {
    //console.log(obj);
    console.log('output: ' + output);
    if(obfuscate && output != ''){
        var outputArea = document.getElementById('output-area');
        outputArea.innerHTML = output;
    } else {
        //val outputArea = document.getElementById('output-area');
        //outputArea.innerHTML = content;
    }
};


/*
collapse0.addEventListener('show.bs.collapse', function () {
    bsCollapse1.hide();
    bsCollapse2.hide();
})

collapse1.addEventListener('show.bs.collapse', function () {
    bsCollapse0.hide();
    bsCollapse2.hide();
})
collapse2.addEventListener('show.bs.collapse', function () {
    bsCollapse0.hide();
    bsCollapse1.hide();
})
*/