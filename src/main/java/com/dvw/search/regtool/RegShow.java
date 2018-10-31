package com.dvw.search.regtool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RegShow extends JFrame{

	Container container;
	static JLabel urlLabel,parserLabel,regLabel,resultLabel,styleLabel,timeLabel,proxyHostLabel,proxyPortLabel;
	static JTextField urlField,regField,timeField,proxyHostField,proxyPortField;
	static JButton parserButton,regButton;
	static JTextArea parserArea,resultArea;
	static JComboBox styleBox,parserBox;
	
	public RegShow()
	{
		super("正则匹配器--author:David.Wang");
		container=getContentPane();
		container.setLayout(new FlowLayout());
		
		//第一部分
		urlLabel=new JLabel("解析URL");
		urlField=new JTextField(46);
		parserButton=new JButton("解析");
		parserButton.addActionListener(new ButtonListener());
		parserBox=new JComboBox();
		parserBox.setMaximumRowCount(3);
		parserBox.addItem("HtmlParser");
		parserBox.addItem("CobraParser");
		parserBox.addItem("Mozswing");

		JPanel p1=new JPanel();
		p1.add(urlLabel);
		p1.add(urlField);
		p1.add(parserBox);
		p1.add(parserButton);
		container.add(p1,BorderLayout.NORTH);
		
		//第二部分
		proxyHostLabel=new JLabel("代理IP");
		proxyHostField=new JTextField(10);
		proxyPortLabel=new JLabel("代理PORT");
		proxyPortField=new JTextField(5);
		JPanel p2=new JPanel();
		p2.add(proxyHostLabel);
		p2.add(proxyHostField);
		p2.add(proxyPortLabel);
		p2.add(proxyPortField);
		container.add(p2);
		
		
		//第三部分
		parserLabel=new JLabel("解析结果");
		parserArea=new JTextArea(15,60);
		//设置自动换行
//		parserArea.setLineWrap(true);
		parserArea.setEditable(false);
		//设置边框
		parserArea.setBorder(BorderFactory.createLineBorder(Color.black));
		//设置文本显示起始点
		parserArea.setCaretPosition(0);
		JScrollPane scroller1=new JScrollPane(parserArea);
		scroller1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel p3=new JPanel();
		p3.add(parserLabel,BorderLayout.AFTER_LAST_LINE);
		p3.add(scroller1);
		container.add(p3,BorderLayout.CENTER);
		
		
		//第四部分
		regLabel=new JLabel("正则表达式");
		regField=new JTextField(35);
		styleLabel=new JLabel("匹配内容");
		styleBox=new JComboBox();
		styleBox.setMaximumRowCount(3);
		styleBox.addItem("数字");
		styleBox.addItem("字符串");
		styleBox.addItem("时间");
		styleBox.addItem("JAVASCRIPT");
		styleBox.addItem("图片url");
		styleBox.addItem("下载链接url");
		JPanel p4=new JPanel();
		p4.add(regLabel);
		p4.add(regField);
		p4.add(styleLabel);
		p4.add(styleBox);
		container.add(p4);
		
		//第五部分		
		timeLabel=new JLabel("时间格式(选择时间匹配时添加内容)");
		timeField=new JTextField(10);
		regButton=new JButton("查看结果");
		regButton.addActionListener(new ButtonListener());
		JPanel p5=new JPanel();
		p5.add(timeLabel);
		p5.add(timeField);
		p5.add(regButton);
		container.add(p5,BorderLayout.CENTER);
		
		//第六部分
		resultLabel=new JLabel("匹配结果");
		resultArea=new JTextArea(10,60);
		//设置自动换行
//		resultArea.setLineWrap(true);
		resultArea.setEditable(false);
		//设置边框
		resultArea.setBorder(BorderFactory.createLineBorder(Color.black));
		//设置文本显示起始点
		resultArea.setCaretPosition(0);
		JScrollPane scroller2=new JScrollPane(resultArea);
		scroller2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel p6=new JPanel();
		p6.add(resultLabel);
		p6.add(scroller2);
		container.add(p6);
		
		setSize(900,700);
		setVisible(true);
	}
	
	class ButtonListener implements ActionListener
	{

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Object source=e.getSource();
			//触发解析事件
			if(source==parserButton){
				String url=urlField.getText();
				if(url.length()==0)
				{
					JOptionPane.showMessageDialog(null,"您没有输入URL\n例如：http://www.sina.com.cn","请输入URL",JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(!url.startsWith("http://")||!url.contains("."))
				{
					JOptionPane.showMessageDialog(null,"URL格式错误，记得添加http://","URL格式错误",JOptionPane.WARNING_MESSAGE);
					return;
				}
				String parserString="";
				//如果选择HtmlParser解析器
				if(parserBox.getSelectedIndex()==0)
				{
					if(styleBox.getSelectedIndex()<3)
						parserString=HtmlParser.getParseredString(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(styleBox.getSelectedIndex()==3)
						parserString=HtmlParser.parserScript(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(styleBox.getSelectedIndex()==4)
						parserString=HtmlParser.parserPicUrl(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(styleBox.getSelectedIndex()==5)
						parserString=HtmlParser.parseDownloadLink(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
						
				}else if(parserBox.getSelectedIndex()==1)
				{
					if(styleBox.getSelectedIndex()<3)
						parserString=CobraHtmlParser.getParseredString(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(styleBox.getSelectedIndex()==3)
						parserString=CobraHtmlParser.parserScript(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(styleBox.getSelectedIndex()==4)
						parserString=CobraHtmlParser.parserPicUrl(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(styleBox.getSelectedIndex()==5)
						parserString=CobraHtmlParser.parseDownloadLink(url,proxyHostField.getText().trim(),proxyPortField.getText().trim());
				}else
				{
					parserString=HtmlParser.parseByMozswing(url);
				}
				
				parserArea.setText(parserString);
			}
			
			//触发正则匹配事件
			if(source==regButton)
			{
				String reg=regField.getText();
				if(parserArea.getText().length()==0)
				{
					JOptionPane.showMessageDialog(null,"您尚未解析URL链接","URL未解析",JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(reg.length()==0)
				{
					JOptionPane.showMessageDialog(null,"您没有输入正则表达式","请输入正则表达式",JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(reg.indexOf("num_of_counter")<0&&styleBox.getSelectedIndex()<=3)
				{
					JOptionPane.showMessageDialog(null,"正则缺少num_of_counter","缺少num_of_counter",JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(styleBox.getSelectedIndex()==0)
				{
					int resultNumber=RegMatch.parserInt(parserArea.getText(), regField.getText());
					if(resultNumber==0)
						resultArea.setText(resultNumber+"\t如果目标数字不为0，那么正则写错了");
					else
						resultArea.setText(resultNumber+"");
				}
				if(styleBox.getSelectedIndex()==1)
				{
					String resultString=RegMatch.parserString(parserArea.getText(), regField.getText());
					if(resultString==null||resultString.length()==0)
						resultArea.setText("对不起，没有匹配到任何内容");
					else
						resultArea.setText(resultString);
				}
				if(styleBox.getSelectedIndex()==2)
				{
					if(timeField.getText().length()==0)
					{
						JOptionPane.showMessageDialog(null,"请输入日期格式：\n例如yyyy-MM-dd hh:mm:ss","请输入日期格式",JOptionPane.WARNING_MESSAGE);
						return;
					}
					String resultDate=RegMatch.parserDate(parserArea.getText(),regField.getText(),timeField.getText());
					if(resultDate==null||resultDate.length()==0)
						resultArea.setText("对不起，没有匹配到任何内容");
					else
						resultArea.setText(resultDate);
				}
				if(styleBox.getSelectedIndex()==3)
				{
					String resultDate=RegMatch.parserString(parserArea.getText(),regField.getText());
					if(resultDate==null||resultDate.length()==0)
						resultArea.setText("对不起，没有匹配到任何内容");
					else
						resultArea.setText(resultDate);
				}
				
				if(styleBox.getSelectedIndex()==4)
				{
					String resultDate=null;
					if(parserBox.getSelectedIndex()==0)
						resultDate=HtmlParser.parserPicUrl(regField.getText(), urlField.getText(),proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(parserBox.getSelectedIndex()==1)
						resultDate=CobraHtmlParser.parserPicUrl(regField.getText(), urlField.getText(),proxyHostField.getText().trim(),proxyPortField.getText().trim());
					
					if(resultDate==null||resultDate.length()==0)
						resultArea.setText("对不起，没有匹配到任何内容");
					else
						resultArea.setText(resultDate);
				}
				if(styleBox.getSelectedIndex()==5)
				{
					String resultDate=null;
					if(parserBox.getSelectedIndex()==0)
						resultDate=HtmlParser.parseDownloadLink(regField.getText(), urlField.getText(),proxyHostField.getText().trim(),proxyPortField.getText().trim());
					else if(parserBox.getSelectedIndex()==1)
						resultDate=CobraHtmlParser.parseDownloadLink(regField.getText(), urlField.getText(),proxyHostField.getText().trim(),proxyPortField.getText().trim());
					
					if(resultDate==null||resultDate.length()==0)
						resultArea.setText("对不起，没有匹配到任何内容");
					else
						resultArea.setText(resultDate);
				}
			}
		}
		
	}
	
	
	public static void main(String[] args)
	{
		RegShow application=new RegShow();
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
