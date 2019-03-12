var chart = null;

window.onload = function() {
	initChart();
}

function setChartTitle(title){
	chart.setTitle({text: title});
}

function setPlotBands(data) {
	data = JSON.parse(data);
	chart.xAxis[0].update({
		plotBands : data
	});
}

function setStateData(data) {
	data = JSON.parse(data);
	chart.series[0].update({
		data : data,
		step : "left"
	});
//	chart.series[0].step = "left";
//	chart.series[0].setData(data);
}

function setValueData(data) {
	data = JSON.parse(data);
	chart.series[0].update({
		data : data,
		step : null
	});
//	chart.series[0].step = null;
//	chart.series[0].setData(data);
}

function initChart() {
	Highcharts.setOptions({
		// 所有语言文字相关配置都设置在 lang 里
		lang : {
			resetZoom : '重置',
			resetZoomTitle : '重置缩放比例',
		}
	});
	chart = Highcharts.chart('c1', {
		title: {
		    text: '设备名称'
		},
		credits : {
			enabled : false
		// 禁用版权信息
		},
		chart : {
			zoomType : 'xy',
			panning : true,
			panKey : 'shift'
		},

		mapNavigation : {
			enabled : true,
			enableButtons : false
		},
		xAxis : {
			type : 'category',
			plotBands : [ {
				"color" : "#FFC1C1",
				"from" : 2,
				"to" : 3
			} ]
		},

		series : [ {
			data : [],
			step : null
		} ]
	});
	// test();
}

function test() {
	var plotBands = [ { // mark the weekend
		color : '#FFC1C1',
		from : 2,
		to : 4,
	}, {
		color : '#FFC1C1',
		from : 5,
		to : 6,
	} ];
	var data = [ {
		'name' : 'x1',
		'y' : 1
	}, {
		'name' : 'x2',
		'y' : 2
	}, {
		'name' : 'x3',
		'y' : 3
	}, {
		'name' : 'x4',
		'y' : 3
	}, {
		'name' : 'x5',
		'y' : 3
	}, {
		'name' : 'x6',
		'y' : 3
	}, {
		'name' : 'x7',
		'y' : 3
	} ];
	chart.series[0].setData(data);
}