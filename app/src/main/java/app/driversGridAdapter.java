package app;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ridesharingmining.www.ridesharingmining.R;
import java.util.List;

/**
 * Created by Ivan on 02/03/2015.
 */
public class driversGridAdapter extends BaseAdapter{
    private Context context;
    private final List<Driver> lDriver;

    public driversGridAdapter(Context context, List<Driver> lDriver) {
        this.setContext(context);
        this.lDriver = lDriver;
    }

    @Override
    public int getCount() {
        return this.getlDriver().size();
    }

    @Override
    public Object getItem(int position) {
        return this.getlDriver().get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.getlDriver().get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.drivers_grid_layout, null);
            // set value into text view
            TextView txtName = (TextView) gridView.findViewById(R.id.txtName);
            txtName.setText(this.getlDriver().get(position).getName());
            TextView txtDistance = (TextView) gridView.findViewById(R.id.txtDistance);
            txtDistance.setText(Double.toString(this.getlDriver().get(position).getDistance())+"km");
            TextView txtTime = (TextView) gridView.findViewById(R.id.txtTime);
            txtTime.setText(Double.toString(this.getlDriver().get(position).getTime())+"min");
            TextView txtRating = (TextView) gridView.findViewById(R.id.txtRating);
            txtRating.setText(Double.toString(this.getlDriver().get(position).getRating())+"-starts");
        } else {
            gridView = (View) convertView;
        }
        return gridView;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<Driver> getlDriver() {
        return lDriver;
    }
}