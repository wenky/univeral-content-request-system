package com.uhg.umvs.bene.cms.contentretrieval.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.lowagie.text.Image;

public class StringToJpg
{

    static ArrayList<BufferedImage> images;

    Image img;

    static int lineHeight;

    static int width = 800;

    static int margin = 5;

    static String fontName = "Arial";

    static int fontSize = 12;

    static Date ts;

    com.lowagie.text.Image i;

    public void Convert(String fNameOnly) throws Exception
    {

        Properties rProp = null;
        ts = new Date((new Date()).getTime() - (24 * 60 * 60 * 1000));
        Graphics2D g, g1;
        FontRenderContext fc;
        String testText = "Testing";
        int fileCnt = 0;
        String outPath = rProp.getProperty("TCSAttachmentFolderPath");

        Font font = null;

        try
        {
            font = new Font(fontName, Font.PLAIN, (int) fontSize);
        }
        catch (Exception e)
        {
            System.out.println("Error opening font file : " + e.getMessage());
        }

        BufferedImage tempBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        g = tempBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // g.setFont(font);
        fc = g.getFontRenderContext();

        Rectangle2D bounds = font.getStringBounds(testText, fc);
        float wrappingWidth = width;
        lineHeight = (int) bounds.getHeight();

        BufferedReader srcFile = null;

        BufferedWriter writer = new BufferedWriter(new FileWriter(outPath + "Log.txt", true));

        try
        {
            srcFile = new BufferedReader(new FileReader(rProp.getProperty("AttachmentFolde rPath") + fNameOnly));
        }
        catch (Exception e)
        {
            System.out.println("Error opening file " + e.getMessage());
            writer.append("Error opening file " + fNameOnly + "!! The file could not be located in the Attachment Folder!!");
        }

        images = new ArrayList<BufferedImage>();
        int lineCnt = margin + margin;

        while (true)
        {
            String line;

            try
            {
                line = srcFile.readLine();

            }
            catch (IOException ignore)
            {
                break;
            }

            if (line == null)
            {
                break;
            }

            if ("".equals(line))
            {
                line = " ";
            }

            AttributedString attribString = new AttributedString(line);
            attribString.addAttribute(TextAttribute.FOREGROUND, Color.WHITE, 0, line.length());
            attribString.addAttribute(TextAttribute.FONT, font, 0, line.length());
            AttributedCharacterIterator aci = attribString.getIterator();
            LineBreakMeasurer lbm = new LineBreakMeasurer(aci, fc);

            while (lbm.getPosition() < line.length())
            {
                BufferedImage lineBuffer = new BufferedImage(width, lineHeight,
                        BufferedImage.TYPE_INT_RGB);
                g1 = lineBuffer.createGraphics();
                g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                TextLayout layout = lbm.nextLayout(wrappingWidth);
                int y = (int) layout.getAscent();
                layout.draw(g1, margin, y);
                images.add(lineBuffer);
                lineCnt += lineHeight;

                /*
                 * New Code
                 * 
                 * images.addAll(images);
                 * 
                 * 
                 * New Code*
                 */

            }

        }

        // if (lineCnt+lineHeight > 480)
        // if (lineCnt+lineHeight > 720)
        // {
        try
        {
            saveImage(outPath + fNameOnly + ".jpg", fileCnt++);
        }
        catch (IOException e)
        {
            System.out.println("Error writing image : " + e.getMessage());
        }

        images.clear();
        lineCnt = margin + margin;
    }

    /* Here is where a number of images are saved and I commented it */
    /*
     * if (lineCnt != 0) { try { saveImage(outPath+fNameOnly+".jpg", fileCnt++);
     * flag=1; } catch (IOException e) {
     * System.out.println("Error writing image : "+e.getMessage()); flag=0; }
     * 
     * }
     * 
     * if (flag==1) {
     * writer.append("File "+fNameOnly+" has been successfully converted to JPG "
     * ); writer.newLine(); }
     * 
     * }
     */

    static void saveImage(String fileName, int fileCnt) throws IOException
    {
        Graphics2D g;
        BufferedImage buffer = new BufferedImage(600, 800, BufferedImage.TYPE_INT_RGB);
        g = buffer.createGraphics();
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, 600, 800);

        for (int i = 0; i < images.size(); i++)
        {
            g.drawImage((BufferedImage) images.get(i), 0, margin + (i * lineHeight), null);
        }

        // ///////////////////////////////
        buffer.flush();
        images.add(buffer);
        // //////////////////////////////

        StringBuffer fullFileName = new StringBuffer(fileName)/*.append(addLeadingZeros(fileCnt, 4))*/.append(".jpg");
        OutputStream out = new FileOutputStream(new File(fullFileName.toString()));
        ImageIO.write(buffer, "jpg", out);
        out.close();

        File imgFile = new File(fullFileName.toString());
        imgFile.setLastModified(ts.getTime());

        ts.setTime(ts.getTime() + (60 * 1000));
    }

    static String addLeadingZeros(int value, int len)
    {
        String strValue = Integer.toString(value);
        String retString = "";

        if (strValue.length() >= len)
        {
            retString = strValue;
        }

        else if (strValue.length() < len)
        {
            retString = "0000000000000000000000000000000".substring(0, len - strValue.length()) + strValue;
        }

        return retString;
    }

}
