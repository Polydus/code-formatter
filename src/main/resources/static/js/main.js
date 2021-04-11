var optionsContainer = document.getElementById('form-options');

var classNames = ['formatter-form-options', 'xml-form-options', 'encode-form-options'];

var selects = [
document.getElementById('setting-select-0'),
document.getElementById('setting-select-1'),
document.getElementById('setting-select-2')
];
var selectsLabels = [
document.getElementById('setting-select-label-0'),
document.getElementById('setting-select-label-1'),
document.getElementById('setting-select-label-2')
];

var classes = [];
classes = classes.concat(Array.from(document.getElementsByClassName(classNames[0])));
classes = classes.concat(Array.from(document.getElementsByClassName(classNames[1])));
classes = classes.concat(Array.from(document.getElementsByClassName(classNames[2])));

var indentSelect = document.getElementById('indent-select-container');
var minifyRadio0 = document.getElementById('minify-radio-0');
var minifyRadio1 = document.getElementById('minify-radio-1');

var xmlrootSelect = document.getElementById('xmlroot-select-container');
var xmlToJsonRadio0 = document.getElementById('xmlToJson-radio-0');
var xmlToJsonRadio1 = document.getElementById('xmlToJson-radio-1');

var desc = document.getElementById('description');

function showIndent(show){
    if(show){
        indentSelect.style.display = 'flex';
    } else {
        indentSelect.style.display = 'none';
    }
}

function showXmlOptions(show){
    if(show){
        xmlrootSelect.style.display = 'flex';
    } else {
        xmlrootSelect.style.display = 'none';
    }
}

minifyRadio0.addEventListener('click',function (){
    beautify = false;
    showIndent(beautify);
});

minifyRadio1.addEventListener('click',function (){
    beautify = true;
    showIndent(beautify);
});

xmlToJsonRadio0.addEventListener('click',function (){
    showXmlRootOption = true;
    showXmlOptions(showXmlRootOption);
});

xmlToJsonRadio1.addEventListener('click',function (){
    showXmlRootOption = false;
    showXmlOptions(showXmlRootOption);
});


selects[0].addEventListener('click',function (){
    showFormOptions(classNames[0]);
    optionsContainer.classList.add('noradius-left');
    setActive(0);
    showIndent(beautify);
});
selects[1].addEventListener('click',function (){
    showFormOptions(classNames[1]);
    optionsContainer.classList.remove('noradius-left');
    setActive(1);
    showIndent(true);
    showXmlOptions(showXmlRootOption);
});
selects[2].addEventListener('click',function (){
    showFormOptions(classNames[2]);
    optionsContainer.classList.remove('noradius-left');
    setActive(2);
});

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


function setActive(index){
    for(var i = 0; i < selectsLabels.length; i++){
        if(i === index){
            selectsLabels[i].classList.add('top-button-active');
        } else {
            selectsLabels[i].classList.remove('top-button-active');
        }
    }

    desc.innerHTML = descs[index];
}

$(selects[0]).hover(
    function(){
        optionsContainer.classList.add('noradius-left');
    },
    function(){
    if(!selects[0].classList.contains('active')){
        optionsContainer.classList.remove('noradius-left');
    }
});


window.onload = function() {
    //console.log(obj);
    //console.log('output: ' + output);
    //console.log(formSettings);
    //console.log(beautify);

    showIndent(beautify);
    showXmlOptions(showXmlRootOption);

    for(var i = 0; i < selects.length; i++){
        if(selects[i].checked){
            desc.innerHTML = descs[i];
            break;
        }
    }



    if(output != ''){
        document.getElementById('output-area').innerHTML = output;
        document.getElementById('output-area-container').scrollIntoView();
    } else {
        //val outputArea = document.getElementById('output-area');
        //outputArea.innerHTML = content;
    }
};




