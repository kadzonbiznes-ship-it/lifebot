/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sourceforge.lept4j.L_Bmf$ByReference
 *  net.sourceforge.lept4j.L_Dna$ByReference
 *  net.sourceforge.lept4j.L_Rch$ByReference
 *  net.sourceforge.lept4j.L_Rcha$ByReference
 *  net.sourceforge.lept4j.L_Rdid$ByReference
 *  net.sourceforge.lept4j.Numa$ByReference
 *  net.sourceforge.lept4j.Numaa$ByReference
 *  net.sourceforge.lept4j.Pix$ByReference
 *  net.sourceforge.lept4j.Pixa$ByReference
 *  net.sourceforge.lept4j.Pixaa$ByReference
 *  net.sourceforge.lept4j.Pta$ByReference
 *  net.sourceforge.lept4j.Ptaa$ByReference
 *  net.sourceforge.lept4j.Sarray$ByReference
 */
package net.sourceforge.lept4j;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.lept4j.L_Bmf;
import net.sourceforge.lept4j.L_Dna;
import net.sourceforge.lept4j.L_Rch;
import net.sourceforge.lept4j.L_Rcha;
import net.sourceforge.lept4j.L_Rdid;
import net.sourceforge.lept4j.Numa;
import net.sourceforge.lept4j.Numaa;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.Pixa;
import net.sourceforge.lept4j.Pixaa;
import net.sourceforge.lept4j.Pta;
import net.sourceforge.lept4j.Ptaa;
import net.sourceforge.lept4j.Sarray;

public class L_Recog
extends Structure {
    public int scalew;
    public int scaleh;
    public int linew;
    public int templ_use;
    public int maxarraysize;
    public int setsize;
    public int threshold;
    public int maxyshift;
    public int charset_type;
    public int charset_size;
    public int min_nopad;
    public int num_samples;
    public int minwidth_u;
    public int maxwidth_u;
    public int minheight_u;
    public int maxheight_u;
    public int minwidth;
    public int maxwidth;
    public int ave_done;
    public int train_done;
    public float max_wh_ratio;
    public float max_ht_ratio;
    public int min_splitw;
    public int max_splith;
    public Sarray.ByReference sa_text;
    public L_Dna.ByReference dna_tochar;
    public IntByReference centtab;
    public IntByReference sumtab;
    public Pixaa.ByReference pixaa_u;
    public Ptaa.ByReference ptaa_u;
    public Numaa.ByReference naasum_u;
    public Pixaa.ByReference pixaa;
    public Ptaa.ByReference ptaa;
    public Numaa.ByReference naasum;
    public Pixa.ByReference pixa_u;
    public Pta.ByReference pta_u;
    public Numa.ByReference nasum_u;
    public Pixa.ByReference pixa;
    public Pta.ByReference pta;
    public Numa.ByReference nasum;
    public Pixa.ByReference pixa_tr;
    public Pixa.ByReference pixadb_ave;
    public Pixa.ByReference pixa_id;
    public Pix.ByReference pixdb_ave;
    public Pix.ByReference pixdb_range;
    public Pixa.ByReference pixadb_boot;
    public Pixa.ByReference pixadb_split;
    public L_Bmf.ByReference bmf;
    public int bmf_size;
    public L_Rdid.ByReference did;
    public L_Rch.ByReference rch;
    public L_Rcha.ByReference rcha;

    public L_Recog() {
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("scalew", "scaleh", "linew", "templ_use", "maxarraysize", "setsize", "threshold", "maxyshift", "charset_type", "charset_size", "min_nopad", "num_samples", "minwidth_u", "maxwidth_u", "minheight_u", "maxheight_u", "minwidth", "maxwidth", "ave_done", "train_done", "max_wh_ratio", "max_ht_ratio", "min_splitw", "max_splith", "sa_text", "dna_tochar", "centtab", "sumtab", "pixaa_u", "ptaa_u", "naasum_u", "pixaa", "ptaa", "naasum", "pixa_u", "pta_u", "nasum_u", "pixa", "pta", "nasum", "pixa_tr", "pixadb_ave", "pixa_id", "pixdb_ave", "pixdb_range", "pixadb_boot", "pixadb_split", "bmf", "bmf_size", "did", "rch", "rcha");
    }

    public L_Recog(Pointer pointer) {
        super(pointer);
        this.read();
    }
}

