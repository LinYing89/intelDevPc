var chart;

window.onload = function() {
	initChart();
}

function setData(data) {
	//字符串一定要转为json对象
	data = JSON.parse(data);
	chart.changeData(data);
}

function initChart() {
	var data = [ {
		"historyTime" : "2019-03-11 16:28:09",
		"value" : 0.0
	}, {
		"historyTime" : "2019-03-11 17:14:41",
		"value" : 0.0
	}, {
		"historyTime" : "2019-03-11 17:14:52",
		"value" : 0.0
	}, {
		"historyTime" : "2019-03-11 17:15:39",
		"value" : 0.0
	}, {
		"historyTime" : "2019-03-11 17:17:29",
		"value" : 0.0
	}, {
		"historyTime" : "2019-03-11 17:25:19",
		"value" : 1.0
	}, {
		"historyTime" : "2019-03-11 17:25:59",
		"value" : -1.0
	}, {
		"historyTime" : "2019-03-11 17:26:06",
		"value" : 1.0
	}, {
		"historyTime" : "2019-03-11 17:26:41",
		"value" : -1.0
	}, {
		"historyTime" : "2019-03-11 17:27:19",
		"value" : 1.0
	}, {
		"historyTime" : "2019-03-11 17:27:39",
		"value" : -1.0
	}, {
		"historyTime" : "2019-03-11 17:28:29",
		"value" : 1.0
	}, {
		"historyTime" : "2019-03-11 17:29:04",
		"value" : -1.0
	}, {
		"historyTime" : "2019-03-11 17:32:57",
		"value" : 1.0
	}, {
		"historyTime" : "2019-03-11 17:33:38",
		"value" : -1.0
	}, {
		"historyTime" : "2019-03-11 17:35:04",
		"value" : 1.0
	}, {
		"historyTime" : "2019-03-11 17:35:49",
		"value" : -1.0
	} ];
	// Step 1: 创建 Chart 对象
	chart = new G2.Chart({
		container : 'c1', // 指定图表容器 ID
		// width : 600, // 指定图表宽度
		forceFit : true, // 宽度自适应
		height : window.innerHeight
	// 指定图表高度
	});
	// Step 2: 载入数据源
	chart.source(data);
	// Step 3：创建图形语法，绘制柱状图，由 genre 和 sold 两个属性决定图形位置，genre 映射至 x 轴，sold 映射至 y 轴
	chart.line().position('historyTime*value');
	chart.point().position('historyTime*value').size(4).shape('circle');
	// Step 4: 渲染图表
	chart.render();
}