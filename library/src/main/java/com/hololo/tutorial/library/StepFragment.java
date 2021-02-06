package com.hololo.tutorial.library;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StepFragment extends Fragment {

    private TextView title;
    private TextView content;
    private TextView summary;
    private ImageView imageView;
    private LinearLayout layout;
    private Button mButton;
    private int buttonEnable;
    private OnButtonClickedListener mListener;

    private Step step;

    static StepFragment createFragment(Step step) {
        StepFragment fragment = new StepFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("step", step);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        step = getArguments().getParcelable("step");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step, container, false);

        initViews(view);
        initData();

        return view;
    }

    private void initData() {
       try {
           title.setText(step.getTitle());
           content.setText(step.getContent());
           summary.setText(step.getSummary());
           imageView.setImageResource(step.getDrawable());
           layout.setBackgroundColor(step.getBackgroundColor());
           mButton.setText(step.getButtonText());
           buttonEnable=step.isButtonEnable();
           if (buttonEnable==0){
               mButton.setVisibility(View.INVISIBLE);
           }
           mButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {

                   mListener.onButtonClicked(view);
               }
           });
       }catch (Exception e){

           e.printStackTrace();
       }
    }

    private void initViews(View view) {
        title = (TextView) view.findViewById(R.id.title);
        content = (TextView) view.findViewById(R.id.content);
        summary = (TextView) view.findViewById(R.id.summary);
        imageView = (ImageView) view.findViewById(R.id.image);
        layout = (LinearLayout) view.findViewById(R.id.container);
        mButton = (Button) view.findViewById(R.id.button);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnButtonClickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnButtonClickedListener ");
        }
    }

    public interface OnButtonClickedListener {
        public void onButtonClicked(View view);
    }

}
