package com.where.tools;

import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.ImageObserver;
import java.awt.*;

public class ImageBuilder {

    private static String SOURCE_FOLDER = "C:\\data\\projects\\wheresmytube\\wheresmytube\\etc\\images\\originals";
    private static String OUT_FOLDER = "C:\\data\\projects\\wheresmytube\\wheresmytube\\etc\\images\\out";

    public static void main(String[] args) {
        try {
            makeFromOriginals();
            //makeCentralCropped();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static void makeCentralCropped()throws IOException{
       File folder = new File(SOURCE_FOLDER);
        new File(OUT_FOLDER).mkdirs();
        File srcFolder = new File(SOURCE_FOLDER, "central.png");

        BufferedImage loadImg = ImageUtil.loadImage(srcFolder.getAbsolutePath());
        ImageUtil.savePng(ImageUtil.crop(loadImg), OUT_FOLDER+"\\central-cropped.png");
    }

    private static void makeFromOriginals() throws IOException{
        File folder = new File(SOURCE_FOLDER);
        new File(OUT_FOLDER).mkdirs();
        File[] files = folder.listFiles();
        for (File pngFile : files) {
            String outFilePrefix = pngFile.getName().substring(0, 1);
            System.out.println("ImageBuilder.makeFromOriginals outFilePrefix is "+outFilePrefix);

            String[] directions = new String[]{"n", "s", "e", "w"};

            final ArrowDawing drawer;
            if(pngFile.getName().equals("northern.png"))
                drawer = new ArrowDawing(Color.WHITE);
            else
                drawer = new ArrowDawing(Color.BLACK);

            for(String dir : directions){
               if(dir.equals("n")){
                  BufferedImage loadImg = ImageUtil.loadImage(pngFile.getAbsolutePath());
                  BufferedImage result = drawer.drawNorthArrow(ImageUtil.crop(loadImg));
                  ImageUtil.savePng(result, OUT_FOLDER+"\\"+outFilePrefix+dir+".png");
               }
               else if(dir.equals("e")){
                  BufferedImage loadImg = ImageUtil.loadImage(pngFile.getAbsolutePath());
                  BufferedImage result = drawer.drawEastArrow(ImageUtil.crop(loadImg));
                  ImageUtil.savePng(result, OUT_FOLDER+"\\"+outFilePrefix+dir+".png");
               }else if(dir.equals("s")){
                  BufferedImage loadImg = ImageUtil.loadImage(pngFile.getAbsolutePath());
                  BufferedImage result = drawer.drawSouthArrow(ImageUtil.crop(loadImg));
                  ImageUtil.savePng(result, OUT_FOLDER+"\\"+outFilePrefix+dir+".png");
               }else if(dir.equals("w")){
                  BufferedImage loadImg = ImageUtil.loadImage(pngFile.getAbsolutePath());
                  BufferedImage result = drawer.drawWestArrow(ImageUtil.crop(loadImg));
                  ImageUtil.savePng(result, OUT_FOLDER+"\\"+outFilePrefix+dir+".png");
               }
            }
        }
    }

    private static interface Publisher{
        void publish();
    }

    private static class ArrowDawing{

        private final Color color;

        public ArrowDawing(Color color) {
            this.color = color;
        }

        private Graphics2D getGraphics(BufferedImage img){
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(color);
            return g;
        }

        private BufferedImage drawNorthArrow(BufferedImage img) {
            Graphics2D g=getGraphics(img);

            // two horizontal lines
            g.drawLine(9, 3, 9, 16);
            g.drawLine(8, 3, 8, 16);

            // pyrymid shape
            g.drawLine(7, 4, 10, 4);
            g.drawLine(6, 5, 11, 5);
            g.drawLine(5, 6, 12, 6);
            g.drawLine(4, 7, 13, 7);
            g.dispose();
            return img;
        }

        private BufferedImage drawSouthArrow(BufferedImage img) {
            Graphics2D g=getGraphics(img);

            // two horizontal lines
            g.drawLine(9, 3, 9, 16);
            g.drawLine(8, 3, 8, 16);

            // pyrymid shape on bottom
            g.drawLine(7, 15, 10, 15);
            g.drawLine(6, 14, 11, 14);
            g.drawLine(5, 13, 12, 13);
            g.drawLine(4, 12, 13, 12);
            g.dispose();
            return img;
        }

        private BufferedImage drawEastArrow(BufferedImage img) {
            Graphics2D g=getGraphics(img);

            // two horizontal lines
            g.drawLine(2, 8, 14, 8);
            g.drawLine(2, 9, 14, 9);

            // pyrymid shape
            g.drawLine(13, 7, 13, 10);
            g.drawLine(12, 6, 12, 11);
            g.drawLine(11, 5, 11, 12);
            g.drawLine(10, 4, 10, 13);

            g.dispose();
            return img;
        }

        private BufferedImage drawWestArrow(BufferedImage img) {
            Graphics2D g=getGraphics(img);

            // two horizontal lines
            g.drawLine(2, 8, 14, 8);
            g.drawLine(2, 9, 14, 9);

            // pyrymid shape
            g.drawLine(3, 7, 3, 10);
            g.drawLine(4, 6, 4, 11);
            g.drawLine(5, 5, 5, 12);
            g.drawLine(6, 4, 6, 13);

            g.dispose();
            return img;
        }
    }

    private static class ImageUtil {

        public static BufferedImage crop(BufferedImage img) {
            int w = img.getWidth();
            int h = img.getHeight();
            int targetWidth = 18;
            int cropWidth = 0 - ((w - targetWidth) / 2);
            BufferedImage result = new BufferedImage(targetWidth, h, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = result.createGraphics();
            g.drawImage(img, cropWidth, 0, null);
            g.dispose();
            return result;
        }

        public static BufferedImage drawNorthArrow(BufferedImage img) {
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(Color.BLACK);

            // two horizontal lines
            g.drawLine(9, 3, 9, 16);
            g.drawLine(8, 3, 8, 16);

            // pyrymid shape
            g.drawLine(7, 4, 10, 4);
            g.drawLine(6, 5, 11, 5);
            g.drawLine(5, 6, 12, 6);
            g.drawLine(4, 7, 13, 7);
            g.dispose();
            return img;
        }

        public static BufferedImage drawSouthArrow(BufferedImage img) {
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(Color.BLACK);

            // two horizontal lines
            g.drawLine(9, 3, 9, 16);
            g.drawLine(8, 3, 8, 16);

            // pyrymid shape on bottom
            g.drawLine(7, 15, 10, 15);
            g.drawLine(6, 14, 11, 14);
            g.drawLine(5, 13, 12, 13);
            g.drawLine(4, 12, 13, 12);
            g.dispose();
            return img;
        }

        public static BufferedImage drawEastArrow(BufferedImage img) {
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(Color.BLACK);

            // two horizontal lines
            g.drawLine(2, 8, 14, 8);
            g.drawLine(2, 9, 14, 9);

            // pyrymid shape
            g.drawLine(13, 7, 13, 10);
            g.drawLine(12, 6, 12, 11);
            g.drawLine(11, 5, 11, 12);
            g.drawLine(10, 4, 10, 13);

            g.dispose();
            return img;
        }

        public static BufferedImage drawWestArrow(BufferedImage img) {
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(Color.BLACK);

            // two horizontal lines
            g.drawLine(2, 8, 14, 8);
            g.drawLine(2, 9, 14, 9);

            // pyrymid shape
            g.drawLine(3, 7, 3, 10);
            g.drawLine(4, 6, 4, 11);
            g.drawLine(5, 5, 5, 12);
            g.drawLine(6, 4, 6, 13);

            g.dispose();
            return img;
        }

        public static BufferedImage loadImage(String ref) throws IOException {
            return ImageIO.read(new File(ref));
        }

        public static void savePng(RenderedImage rendImage, String file) throws IOException {
            ImageIO.write(rendImage, "png", new File(file));
        }

        public static void saveJpeg(String ref, BufferedImage img) {
            BufferedOutputStream out;

            try {
                out = new BufferedOutputStream(new FileOutputStream(ref));
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);

                JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
                int quality = 5;
                quality = Math.max(0, Math.min(quality, 100));
                param.setQuality((float) quality / 100.0f, false);
                encoder.setJPEGEncodeParam(param);
                encoder.encode(img);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

//    public void loadAndDisplayImage(JFrame frame) {
//        // Load the img
//        try {
//            BufferedImage loadImg = ImageUtil.loadImage(file);
//            frame.setBounds(0, 0, loadImg.getWidth(), loadImg.getHeight());
//            // Set the panel visible and add it to the frame
//            frame.setVisible(true);
//            // Get the surfaces Graphics object
//            Graphics2D g = (Graphics2D) frame.getRootPane().getGraphics();
//            // Now crop the image
//            g.drawImage(loadImg, null, 0, 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    

//    public static class JImagePanel extends JPanel {
//        private BufferedImage image;
//        int x, y;
//
//        public JImagePanel(BufferedImage image, int x, int y) {
//            super();
//            this.image = image;
//            this.x = x;
//            this.y = y;
//        }
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            g.drawImage(image, x, y, null);
//        }
//    }


}
