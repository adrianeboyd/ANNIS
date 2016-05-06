//Connector
window.annis_visualizers_component_visjs_VisJsComponent = function() {

var outerDiv = this.getElement();

outerDiv.innerHTML = "<div id='myDiv'></div>" +
		"<div id='myLegend'></canvas>";

//var div = this.getElement(); 
var div =outerDiv.getElementsByTagName("div")[0];

//var containerWidth = $(div).parent().width();
//var containerHeight =  $(div).parent().height();



var container = div;

$(container).css({"width": "100%", "height": "90%"});
var	containerWidth = $(container).width();
var	containerHeight =  $(container).height();

div.style.border = "thin solid red";
div.style.display = "inline-block";


var legend_container = outerDiv.getElementsByTagName("div")[1];
$(legend_container).css({"width": "100%", "height": "10%"});


//$(container).css({"width": containerWidth, "height": containerHeight});

var strNodes =  this.getState().strNodes;
var strEdges = this.getState().strEdges;
var visjscomponent = null;


// Handle changes from the server-side
this.onStateChange = function(){
		
	// cleanup old graph
    if(typeof visjscomponent !== 'undefined' && visjscomponent != null){
    	visjscomponent.destroy();
    }
	
    strNodes = this.getState().strNodes;
    strEdges = this.getState().strEdges;
    this.init(strNodes, strEdges);
    };


this.init = function(strNodes, strEdges){

var json_nodes = JSON.parse(strNodes);
var json_edges = JSON.parse(strEdges);


var nodeDist;
var sprLength = 100;
var sprConstant;

var nNodes = json_nodes.length;

if (nNodes < 20){
	nodeDist = 100;
	sprConstant = 1.2;
} else if (nNodes >=20 && nNodes < 100){
	nodeDist = 120;
	sprConstant = 1.0;

} else if (nNodes >= 100 && nNodes < 400) {
	nodeDist = 150;
	sprConstant = 0.8;
} else {
	nodeDist = 180;
	sprConstant = 0.6;
};



var data = {
nodes: json_nodes,
edges: json_edges
};
var options = {
nodes:{
shape: "box"
},
edges: {
smooth: true,
arrows: {
to: {
enabled: true
}
}
},
interaction: {
  navigationButtons: true,
  keyboard: true
        },
layout: {
hierarchical:{
     direction: 'UD',
     sortMethod: 'directed'
}
} ,
physics: {
hierarchicalRepulsion: {
centralGravity: 0.8,
springLength: sprLength,
springConstant: sprConstant,
nodeDistance: nodeDist,
damping: 0.04
},
maxVelocity: 50,
minVelocity: 1,
solver: 'hierarchicalRepulsion',
timestep: 0.5,
stabilization: {
iterations: 1000
}
}
}
;
$(container).remove("canvas");


visjscomponent = new vis.Network(container, data, options); 



$("div.vis-network div.vis-navigation div.vis-button.vis-up").css({"bottom": "50px", "left": "auto", "right": "55px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-down").css({"bottom": "10px", "left": "auto", "right": "55px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-left").css({"bottom": "10px", "left": "auto", "right": "95px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-right").css({"bottom": "10px", "left": "auto", "right": "15px"});

$("div.vis-network div.vis-navigation div.vis-button.vis-zoomIn").css({"bottom": "auto", "top": "10px", "left": "auto", "right": "15px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-zoomOut").css({"bottom": "auto", "top": "50px", "left": "auto", "right": "15px"});
$("div.vis-network div.vis-navigation div.vis-button.vis-zoomExtends").css({"bottom": "auto", "top": "10px", "left": "auto", "right": "55px"});

drawLegend();
  
};


window.addEventListener("resize", function(){
	$(container).css({"width": "100%", "height": "90%"});
	containerWidth = $(container).width();
	containerHeight =  $(container).height();
	visjscomponent.setSize(containerWidth, containerHeight);
	visjscomponent.fit();


}); 


function drawLegend() {
//var legend_container = outerDiv.getElementsByTagName("canvas")[1];

var legend_nodes = 
[{"id":"legendToken","label":"Token","color":"#CCFF99","x":0,"physics":"false","level":0}
,{"id":"legendSpan","label":"Spanning Node","color":"#A9D0F5","x":150,"physics":"false","level":0}
,{"id":"legendStructure","label":"Structure Node","color":"#FFCC00","x":300,"physics":"false","level":0}
];
var legend_edges = [];
var data = {
nodes: legend_nodes,
edges: legend_edges
};

var options = {
nodes:{
shape: "box"
},
layout: {
hierarchical:{
     direction: 'UD',
     sortMethod: 'directed'
	}
 }
};
$(legend_container).remove("canvas");


legend = new vis.Network(legend_container, data, options); 

};

};
