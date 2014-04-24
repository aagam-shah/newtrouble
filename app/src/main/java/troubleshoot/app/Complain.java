package troubleshoot.app;

/**
 * Created by Aagam Shah on 28/3/14.
 * Class for the structure of the complain
 */
public class Complain {
    public String title = "";
    public String descr = "";
    public String locality = "";
    public String imgloc = "";
    public String imgol = "";
    public String category = "";
    public String status = "";
    public String date = "";
    public String reviewer = "";
    public String reviewer_c = "";
    public String officer = "";
    public String officer_c = "";
    public int complainid = 0;

    /**
     * Complain with all the attributes
     */
    public Complain(int cid, String title, String desc, String status, String date,
                    String loca, String imgloc, String imgol, String cat) {

        this.title = title;
        this.descr = desc;
        this.locality = loca;
        this.imgloc = imgloc;
        this.imgol = imgol;
        this.category = cat;
        this.status = status;
        this.date = date;
        this.complainid = cid;

    }


    public Complain(int cid, String title, String status,
                    String loca, String imgloc, String imgol) {

        this.title = title;
        this.locality = loca;
        this.imgloc = imgloc;
        this.imgol = imgol;
        this.status = status;
        this.complainid = cid;

    }

    /**
     * Complain with the information of the admin details
     *
     */
    public Complain(int complainid, String title, String status, String locality,
                    String img_l, String img_ol, String date, String reviewer, String reviewer_c,
                    String officer, String officer_c) {

        this.title = title;
        this.locality = locality;
        this.imgloc = img_l;
        this.imgol = img_ol;
        this.status = status;
        this.complainid = complainid;
        this.date = date;
        this.reviewer = reviewer;
        this.reviewer_c = reviewer_c;
        this.officer = officer;
        this.officer_c = officer_c;

    }
}
