package com.intalio.web.repository.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.intalio.web.profile.IDiagramProfile;
import com.intalio.web.repository.IUUIDBasedRepository;

public class UUIDBasedDroolsRepository implements IUUIDBasedRepository {

    private static final Logger _logger = Logger.getLogger(UUIDBasedDroolsRepository.class);
    private final static String DEFAULTS_PATH = "defaults";
  
    
    private String _defaultsPath;
    private UUIDBasedFileRepository fileRep = new UUIDBasedFileRepository();
    
    @Override
    public void configure(HttpServlet servlet) {
        _defaultsPath = servlet.getServletContext().getRealPath("/" + DEFAULTS_PATH);
        fileRep.configure(servlet);
    }

    @Override
    public byte[] load(HttpServletRequest req, String uuid, String ext, String loadExt, String usr, String pwd) {
        String inputLine;
        String processjson = "";
        
        try {
            // check with Guvnor to see what it has for this uuid for us
            processjson = doHttpUrlConnectionAction(loadExt + "?uuid=" + uuid + "&usr=" + usr + "&pwd=" + pwd);
            System.out.println("***** json from guvnor: " + processjson);
            if(processjson.length() > 0) {
                return displayProcess(processjson);
            } else {
                return displayDefaultProcess();
            }
        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(HttpServletRequest req, String uuid, String json,
            String svg, IDiagramProfile profile, Boolean autosave) {
        fileRep.save(req, uuid, json, svg, profile, autosave);
        System.out.println("UUIDBasedDroolsRepository.save().");
    }
    
    private byte[] displayDefaultProcess() throws Exception {
        String  filename = _defaultsPath + "/BPMN2-DefaultProcess.json";
        InputStream input = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            input = new FileInputStream(filename);
            byte[] buffer = new byte[4096];
            int read;
           
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } catch (FileNotFoundException e) {
            //unlikely since we just checked.
            _logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
            
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (input != null) { try { input.close();} catch(Exception e) {} }
        }
        return output.toByteArray();
    }
    
    private byte[] displayProcess(String json) throws Exception {
        InputStream input = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try {
            input = new ByteArrayInputStream(json.getBytes("UTF-8"));
            byte[] buffer = new byte[4096];
            int read;
           
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } catch (FileNotFoundException e) {
            //unlikely since we just checked.
            _logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
            
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (input != null) { try { input.close();} catch(Exception e) {} }
        }
        return output.toByteArray();       
    }
    private String doHttpUrlConnectionAction(String desiredUrl) throws Exception {
      URL url = null;
      BufferedReader reader = null;
      StringBuilder stringBuilder;

      try {
        url = new URL(desiredUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("GET");
        
        connection.setReadTimeout(5*1000);
        connection.connect();

        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        stringBuilder = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null) {
          stringBuilder.append(line + "\n");
        }
        return stringBuilder.toString();
      } catch (Exception e) {
          _logger.error(e.getMessage(), e);
          // don't blow up, we will just show the default process
          return "";
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException ioe) {
              _logger.error(ioe.getMessage(), ioe);
           // don't blow up, we will just show the default process
              return "";
          }
        }
      }
    }

}
