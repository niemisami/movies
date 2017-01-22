package com.niemisami.movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sami on 15.1.2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private List<Movie> mMovies;

    final private OnMovieAdapterItemClickListener mMovieItemClickListener;

    public MovieAdapter(OnMovieAdapterItemClickListener movieItemClickListener) {
        mMovieItemClickListener = movieItemClickListener;
    }

    public interface OnMovieAdapterItemClickListener {
        void onMovieItemClickListener(View view, int itemPosition);
    }

    public void setMovies(List<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_list_item, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        if (mMovies != null) {
            final Movie movie = mMovies.get(position);
            final MovieAdapterViewHolder movieAdapterViewHolder = holder;
            movieAdapterViewHolder.displayPosterGradient(false);
            movieAdapterViewHolder.titleTextView.setText("");
            Context context = movieAdapterViewHolder.posterImageView.getContext();
            Picasso.with(context)
                    .load(NetworkUtils.buildPosterUri("w342", movie.getPosterPath().substring(1)))
                    .error(R.mipmap.ic_launcher)
                    .into(holder.posterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            movieAdapterViewHolder.displayPosterGradient(true);
                            movieAdapterViewHolder.titleTextView.setText(movie.getTitle());
                        }

                        @Override
                        public void onError() {

                        }
                    });

        }
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) {
            return 0;
        }
        return mMovies.size();
    }

    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected ImageView posterImageView;
        protected TextView titleTextView;
        View posterGradient;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            posterImageView = (ImageView) itemView.findViewById(R.id.poster_view);
            titleTextView = (TextView) itemView.findViewById(R.id.movie_title_label);
            posterGradient = itemView.findViewById(R.id.poster_gradient);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int itemPosition = getAdapterPosition();
            mMovieItemClickListener.onMovieItemClickListener(view, itemPosition);
        }

        public void displayPosterGradient(boolean display) {
            if(display) {
                posterGradient.setVisibility(View.VISIBLE);
            } else
                posterGradient.setVisibility(View.INVISIBLE);

        }
    }
}
