package com.lsscl.app.bean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleInsets;

import com.lsscl.app.bean2.IImage;
import com.serotonin.mango.rt.dataImage.PointValueTime;

/**
 * 24小时点数据统计实体
 * 
 * @author yxx
 * 
 */
public class PointsWithin24MsgBody extends MsgBody implements IImage{
	private static final long serialVersionUID = -3693167843596930549L;
	private List<PointValueTime> points;
	private String title;
	private String subTitle;
	private int dataType;// 数据类型
	private String imageType;// 统计图类型 bar：柱形图，line（默认）：折线图
	private long stime;


	public long getStime() {
		return stime;
	}

	public void setStime(long stime) {
		this.stime = stime;
	}

	public List<PointValueTime> getPoints() {
		return points;
	}

	public void setPoints(List<PointValueTime> points) {
		this.points = points;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String toJSON(){
		StringBuilder sb = new StringBuilder();
		String [] arr = new String[4320];
		for(PointValueTime p:points){
			int i = (int) ((p.getTime()-stime)/20000);
			arr[i] = p.getValue().toString();
		}
		sb.append("\"MSGBODY\":{\"STIME\":"+stime+",\"POINTS\":[");
		for(String v:arr){
			String s = v!=null?v:""+0;;
			sb.append(s+",");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		sb.append("]}");
		return sb.toString();
	}
	
	/**
	 * 生成统计图片
	 */
	public void toImage(OutputStream out) {
		System.out.println("count:"+points.size());
		// 建立JFreeChart
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series = new TimeSeries(subTitle, Millisecond.class);
		double max = 0;
		double min = 0;
		int i = 0;
		for (PointValueTime point:points) {
			Double value = point.getDoubleValue();
			if (i == 0) {
				max = value;
				min = value;
			}
			if (value > max)
				max = value;
			if (value < min)
				min = value;
			Millisecond millisecond = new Millisecond(new Date(
					 point.getTime()));
			series.addOrUpdate(millisecond, value);
			i++;
		}
		dataset.addSeries(series);
		JFreeChart jfc = null;
		if ("line".equals(imageType)||imageType==null) {
			jfc = ChartFactory.createTimeSeriesChart("点统计", " ",
					null, dataset, false, // 显示图例
					true, // 采用标准生成器
					false // 是否生成超链接
					);
		} else if ("bar".equals(imageType)) {
			jfc = ChartFactory.createXYBarChart("点统计", " ",true,
					"", dataset, PlotOrientation.VERTICAL,
					false, // 显示图例
					true, // 采用标准生成器
					false // 是否生成超链接
					);
		}
		// 设置标题并且设置其字体，防止中文乱码
		TextTitle textTitle = new TextTitle("                    " + title);
		textTitle.setFont(new Font("微软雅黑", Font.PLAIN, 10));
		textTitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
		jfc.setTitle(textTitle);
		// 设置图表子标题
		TextTitle subTitleText = new TextTitle("                   " + subTitle);
		subTitleText.setFont(new Font("微软雅黑", Font.PLAIN, 17));
		subTitleText.setHorizontalAlignment(HorizontalAlignment.LEFT);
		jfc.addSubtitle(subTitleText);
		XYPlot plot = (XYPlot) jfc.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		if (plot.getRenderer() instanceof LineAndShapeRenderer) {
			LineAndShapeRenderer render = (LineAndShapeRenderer) plot
					.getRenderer();
			render.setBaseShapesVisible(true);
			render.setBaseShapesFilled(true);
			render.setSeriesOutlineStroke(0, new BasicStroke(0.3F));// 设置折点的大小
		}
		if(plot.getRenderer() instanceof XYBarRenderer){//去除柱形图的阴影
			XYBarRenderer xyBarRenderer = (XYBarRenderer) plot.getRenderer();
			xyBarRenderer.setShadowVisible(false);
			xyBarRenderer.setBarPainter(new StandardXYBarPainter());
		}
		// x轴
		Axis xAxis = plot.getDomainAxis();
		Font font = new Font("微软雅黑", Font.BOLD, 15);
		Font font2 = new Font("微软雅黑", Font.BOLD, 15);
		xAxis.setTickLabelFont(font);
		xAxis.setLabelFont(font2);

		// y轴
		ValueAxis yAxis = plot.getRangeAxis();
		if(max!=min){
			yAxis.setRange(min - (max - min) / 2/*0*/, max + (max - min) / 2/*max*1.2*/);
		}else{
			yAxis.setRange(0,max*1.2);
		}
		yAxis.setTickLabelFont(font);
		yAxis.setLabelFont(font2);

		try {
			ChartUtilities.writeChartAsPNG(out, jfc, 800, 400);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
