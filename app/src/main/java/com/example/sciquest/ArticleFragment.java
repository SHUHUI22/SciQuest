package com.example.sciquest;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

public class ArticleFragment extends Fragment {

    private ImageButton BtnBack;
    private TextView TVArticleTittle;
    private Spinner spinnerFontSize, spinnerFontColor, spinnerBackgroundColor;
    private NestedScrollView NSVArticleBg;
    private String currentBgColor, currentFontColor, articleContent;
    private int currentFontSize;
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private WebView WVArticleContent;


    public ArticleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext(); // Get context in onViewCreated
        if (context == null) return; // Ensure context is not null

        TVArticleTittle = view.findViewById(R.id.TVArticleTittle);
        WVArticleContent = view.findViewById(R.id.WVArticleContent);
        BtnBack = view.findViewById(R.id.BtnBack);
        spinnerFontSize = view.findViewById(R.id.spinnerFontSize);
        spinnerFontColor = view.findViewById(R.id.spinnerFontColor);
        spinnerBackgroundColor = view.findViewById(R.id.spinnerBackgroundColor);
        NSVArticleBg = view.findViewById(R.id.NSVArticleBg);

        setupFontSizeSpinner();
        setupFontColorSpinner();
        setupBackgroundColorSpinner();

        currentBgColor = getColorHexFromName("White Bg",context); // Default background color
        currentFontColor = getColorHexFromName("Black Text",context); // Default font color
        currentFontSize = 12; // Default font size

        // Enable JavaScript for better rendering
        WVArticleContent.getSettings().setJavaScriptEnabled(true);
        WVArticleContent.setVerticalScrollBarEnabled(true);

        // Get the passed arguments from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String topicTitle = bundle.getString("topicTitle");
            TVArticleTittle.setText(topicTitle);

            // Fetch article content
            db.collection("Article")
                    .document(topicTitle.replace(" ", ""))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            articleContent = documentSnapshot.getString("content");
                            loadArticleContent();

                        } else {
                            // Handle case where the document doesn't exist
                                Log.e("Firestore", "Document not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching article: " + e.getMessage());
                    });
        }

        // Font Size Spinner Listeners
        spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fontSize = parent.getItemAtPosition(position).toString().replace("px", "");
                currentFontSize = Integer.parseInt(fontSize);
                loadArticleContent(); // Reload the content with new font size
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Font Color Spinner Listener
        spinnerFontColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String colorName = parent.getItemAtPosition(position).toString();
                currentFontColor = getColorHexFromName(colorName,context);
                loadArticleContent(); // Reload the content with new font color
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Background Color Spinner Listener
        spinnerBackgroundColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String colorName = parent.getItemAtPosition(position).toString();
                currentBgColor = getColorHexFromName(colorName,context);
                loadArticleContent(); // Reload the content with new background color
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // Obtain NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.NHFMain);

        // Hide the toolbar
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
        }

        // Navigate back
        BtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack(R.id.DestArticleList, true);
                navController.navigate(R.id.DestArticleList);
            }
        });

    }

    private void setupFontSizeSpinner() {
        ArrayAdapter<CharSequence> fontSizeAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.font_sizes,
                android.R.layout.simple_spinner_item
        );
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(fontSizeAdapter);

    }

    private void setupFontColorSpinner() {
        ArrayAdapter<CharSequence> fontColorAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.font_colors,
                android.R.layout.simple_spinner_item
        );
        fontColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontColor.setAdapter(fontColorAdapter);

    }

    private void setupBackgroundColorSpinner() {
        ArrayAdapter<CharSequence> bgColorAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.bg_colors,
                android.R.layout.simple_spinner_item
        );
        bgColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBackgroundColor.setAdapter(bgColorAdapter);
    }

    private String getColorHexFromName(String colorName, Context context) {
        int colorResId;
        switch (colorName) {
            case "Black Text":
                colorResId = R.color.black;
                break;
            case "Red Text":
                colorResId = R.color.red;
                break;
            case "Green Text":
                colorResId = R.color.green;
                break;
            case "Blue Text":
                colorResId = R.color.blue;
                break;
            case "Purple Text":
                colorResId = R.color.purple;
                break;
            case "Orange Text":
                colorResId = R.color.orange;
                break;
            case "Grey Text":
                colorResId = R.color.grey;
                break;
            case "White Bg":
                colorResId = R.color.white;
                break;
            case "Light Grey Bg":
                colorResId = R.color.lightGrey;
                break;
            case "Black Bg":
                colorResId = R.color.black;
                break;
            case "Yellow Bg":
                colorResId = R.color.yellow;
                break;
            case "Light Blue Bg":
                colorResId = R.color.lightBlue;
                break;
            case "Light Purple Bg":
                colorResId = R.color.lightPurple;
                break;
            case "Peach Bg":
                colorResId = R.color.secondaryColor;
                break;
            default:
                colorResId = R.color.secondaryColor;
                break;
        }
        // Convert the resolved color to a hexadecimal string
        int colorValue = ContextCompat.getColor(context, colorResId);
        return String.format("#%06X", (0xFFFFFF & colorValue));
    }

    private void loadArticleContent() {
        if (articleContent == null || articleContent.isEmpty()) {
            return;
        }
        NSVArticleBg.setBackgroundColor(android.graphics.Color.parseColor(currentBgColor));

        String styledContent = "<html><head><style>" +
                "body { font-size: " + currentFontSize + "px; color: " + currentFontColor + "; background-color: " + currentBgColor + "; }" +
                "</style></head><body>" + articleContent + "</body></html>";

        WVArticleContent.loadDataWithBaseURL(null, styledContent, "text/html", "UTF-8", null);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Show the toolbar when navigating back
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
        }
    }

}