package com.example.sotsuken_app;
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page1, container, false);

        Button btn = view.findViewById(R.id.buttonNext);
        btn.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new NextFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
