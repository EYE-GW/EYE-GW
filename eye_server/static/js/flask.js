


/*

$(document).ready(function(){ 
	$('#button').click(function(){ 
		$.ajax({ 
			type:"GET", 
			url:"http://127.0.0.1:5000/static/json/music.json", 
			dataType:"json", 	
			success:function(data){ 			
				var music="</ul>";  
				var UrlFormat="";
				for (var i=0; i<3; i++){
					UrlFormat=UrlFormat+"/"+data[i].optionValue;
				}
				music="<img src=http://127.0.0.1:5000"+UrlFormat+">";	
				music+="</ul>"; 
				$('#result').append(music); 
			} 

		}); 
		return false; 
	}); 
}); 
*/



/*
function getImg(callback){
	var webImg = document.querySelector('.right ul li:first-child img');
	webImg.src = 'http://127.0.0.1:5000/static/images/404.png';
}
*/

/*

var webImg = document.querySelector('.right ul li:first-child img');
webImg.onclick = function(){
	var imgSrc = webImg.getAttribute('src');
	if (imgSrc === '/static/images/1.png'){
		webImg.setAttribute('src','http://127.0.0.1:5000/static/images/404.png');
		}else {
			webImg.setAttribute('src','/static/images/1.png');
		
		}


}

/*
function getImg(callback){
	var webImg = document.querySelector('.right ul li:first-child img');
	$.ajax({ 
		type:"GET", 
		url:"http://127.0.0.1:5000/static/json/music.json", 
		dataType:"json", 
		success:function(data){
				var UrlFormat = "";
				for (var i=0; i<3; i++){
					UrlFormat=UrlFormat+"/"+data[i].optionValue;
				}
				callback(UrlFormat);
			} 	
	});
	getImg(function(data){
	var UrlLink = console.dir(data);
	});
	webImg.src="http://127.0.0.1:5000"+UrlLink;
}
*/

 function getImg(){
	var webImg = document.querySelector('.right ul li:first-child img');
	var UrlFormat = "";
	$.ajax({ 
		type:"GET", 
		url:"http://127.0.0.1:5000/static/json/music.json", 
		dataType:"json", 
		success:function(data){				
				for (var i=0; i<3; i++){
					UrlFormat=UrlFormat+"/"+data[i].optionValue;
				}
			} 	
	});
	webImg.src="http://127.0.0.1:5000"+UrlFormat;
}


