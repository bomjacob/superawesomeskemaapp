package dk.aarhustech.edu.rainbow.horario;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.Arrays;


public class MapView extends SubsamplingScaleImageView {
    private static final String TAG = MapView.class.getSimpleName();
    private final Paint paint = new Paint();
    private OnClickLocationListener onClickLocationListener;
    private String floor;
    private MapFragment.Room[] rooms;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();

        final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                if (isReady() && onClickLocationListener != null) {
                    onClickLocationListener.onClickLocation(viewToSourceCoord(event.getX(), event.getY()));
                }
                return true;
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    public void setRooms(MapFragment.Room[] rooms) {
        this.rooms = rooms;
        if (!floor.equals(rooms[0].getFloor())) {
            setFloor(rooms[0].getFloor());
        }
        initialise();
        invalidate();
    }

    private void initialise() {
        paint.setColor(getResources().getColor(R.color.highlight));
        paint.setStyle(Paint.Style.FILL);

//        float density = getResources().getDisplayMetrics().densityDpi;
//        pin =  BitmapFactory.decodeResource(this.getResources(), R.drawable.pin);
//        float w = (density/4000f) * pin.getWidth();
//        float h = (density/4000f) * pin.getHeight();
//        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    public void setFloor(String name) {
        floor = name;
        setImage(ImageSource.asset("maps/" + name + ".png"));
        rooms = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady()) {
            return;
        }

        //paint.setAntiAlias(true);

        if (rooms != null) {
            for (MapFragment.Room room : rooms) {
                drawRoom(canvas, room);
            }
        }
    }

    private void drawRoom(Canvas canvas, MapFragment.Room room) {
        PointF[] points = room.getPoints();
        Path path = new Path();
        PointF start = sourceToViewCoord(points[0]);
        path.reset();
        path.moveTo(start.x, start.y);
        for (int i = 1; i < points.length; i++) {
            PointF p = sourceToViewCoord(points[i]);
            path.lineTo(p.x, p.y);
        }
        path.lineTo(start.x, start.y);
        path.close();
        canvas.drawPath(path, paint);
    }

    void setOnClickLocationListener(OnClickLocationListener listener) {
        this.onClickLocationListener = listener;
    }

    public String getFloor() {
        return floor;
    }

    interface OnClickLocationListener {
        void onClickLocation(PointF point);
    }
}
