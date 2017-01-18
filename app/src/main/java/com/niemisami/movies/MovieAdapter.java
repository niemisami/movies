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
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sami on 15.1.2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private List<Movie> mMovies;

    public MovieAdapter() {
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
            Movie movie = mMovies.get(position);

            Context context = holder.posterImageView.getContext();
            Picasso.with(context)
                    .load(NetworkUtils.buildPosterUri(movie.getPosterPath()))
                    .error(R.mipmap.ic_launcher)
                    .into(holder.posterImageView);

            holder.titleTextView.setText(movie.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) {
            return 0;
        }
        return mMovies.size();
    }

    class MovieAdapterViewHolder extends RecyclerView.ViewHolder {

        protected ImageView posterImageView;
        protected TextView titleTextView;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            posterImageView = (ImageView) itemView.findViewById(R.id.poster_view);
            titleTextView = (TextView) itemView.findViewById(R.id.movie_title_label);
        }
    }
}
