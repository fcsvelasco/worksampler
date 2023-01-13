package com.example.marasigan.worksampler.entities;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ProjectDebugMode extends Project {
    public final static String FILE_NAME_DEBUG_MODE = "project-debug-mode.txt";

    public ProjectDebugMode(int mode) {
        super(mode);
    }

    @Override
    public void save(Context context){
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME_DEBUG_MODE, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
//            Toast.makeText(context, "Project saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Context context){
        File file = context.getFileStreamPath(FILE_NAME_DEBUG_MODE);
        boolean deleted = file.delete();
    }
}
