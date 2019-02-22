package org.mp4parser;

import android.support.annotation.NonNull;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MetaBox;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;

public class BetterMetaBox extends MetaBox {

    private IlstBox ilstBox;
    private KeysBox keysBox;

    @Override
    public void setBoxes(List<Box> boxes) {
        super.setBoxes(boxes);
        ilstBox = Path.getPath(this, IlstBox.TYPE);
        keysBox = Path.getPath(this, KeysBox.TYPE);
    }

    public void addTags(Map<String, String> tags) {
        if (tags.isEmpty())
            return;

        if ((ilstBox = Path.getPath(this, IlstBox.TYPE)) == null) {
            ilstBox = new IlstBox();
            addBox(ilstBox);
        }

        if ((keysBox = Path.getPath(this, KeysBox.TYPE)) == null) {
            keysBox = new KeysBox();
            addBox(keysBox);
        }

        //ensure we have equal amount of keys and ilst
        while (keysBox.getBoxes().size() < ilstBox.getBoxes().size()) {
            keysBox.addBox(new MdtaBox(" "));
        }
        while (keysBox.getBoxes().size() > ilstBox.getBoxes().size()) {
            ilstBox.addBox(new DataBox(0, " "));
        }

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String value = entry.getValue();
            int pos = findBoxPositionByTag(entry.getKey());
            if (pos >= 0) {
                Box box = ilstBox.getBoxes().get(pos);
                if (box instanceof DataBox) {
                    ((DataBox) box).setData(0, value);
                }
            } else {
                keysBox.addBox(new MdtaBox(entry.getKey()));
                ilstBox.addBox(new DataBox(0, value));
            }
        }
    }

    protected int findBoxPositionByTag(@NonNull String tag) {
        List<Box> keysBoxes = keysBox.getBoxes();
        for (int i = 0; i < keysBoxes.size(); i++) {
            Box box = keysBoxes.get(i);
            if (box instanceof MdtaBox) {
                if (!((MdtaBox) box).isParsed())
                    ((MdtaBox) box).parseDetails();
                String key = ((MdtaBox) box).getKey();
                if (tag.equals(key))
                    return i;
            }
        }
        return -1;
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        super.getBox(writableByteChannel);
    }
}